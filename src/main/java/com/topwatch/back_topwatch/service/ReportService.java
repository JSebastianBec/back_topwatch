package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.ContentReport;
import com.topwatch.back_topwatch.domain.Item;
import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.domain.WatchList;
import com.topwatch.back_topwatch.domain.enums.ReportStatus;
import com.topwatch.back_topwatch.domain.enums.ReportTargetType;
import com.topwatch.back_topwatch.dto.CreateReportRequest;
import com.topwatch.back_topwatch.dto.ReportResponse;
import com.topwatch.back_topwatch.exception.BadRequestException;
import com.topwatch.back_topwatch.exception.ResourceNotFoundException;
import com.topwatch.back_topwatch.repository.ContentReportRepository;
import com.topwatch.back_topwatch.repository.ItemRepository;
import com.topwatch.back_topwatch.repository.UserRepository;
import com.topwatch.back_topwatch.repository.WatchListRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ContentReportRepository contentReportRepository;
    private final WatchListRepository watchListRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final WatchListService watchListService;
    private final ItemService itemService;
    private final UserService userService;
    private final ImageStorageService imageStorageService;

    /** Reporting the same target again while the first report is open changes nothing. */
    @Transactional
    public void create(@NonNull CreateReportRequest request, @NonNull User reporter) {
        Long ownerId = findOwnerId(request.targetType(), request.targetId());
        if (reporter.getId().equals(ownerId)) {
            throw new BadRequestException("You cannot report your own content");
        }
        boolean alreadyOpen = contentReportRepository.existsByReporter_IdAndTargetTypeAndTargetIdAndStatus(
                reporter.getId(), request.targetType(), request.targetId(), ReportStatus.OPEN);
        if (alreadyOpen) return;

        contentReportRepository.save(ContentReport.builder()
                .reporter(reporter)
                .targetType(request.targetType())
                .targetId(request.targetId())
                .reason(request.reason())
                .details(StringUtils.hasText(request.details()) ? request.details().trim() : null)
                .status(ReportStatus.OPEN)
                .build());
    }

    /** Open reports, oldest first. */
    @Transactional(readOnly = true)
    public List<ReportResponse> findOpenReports() {
        return contentReportRepository.findByStatusOrderByCreationDateAsc(ReportStatus.OPEN).stream()
                .map(report -> ReportResponse.from(report, findTargetName(report)))
                .toList();
    }

    /** Closes the report and any other about the same target, keeping the content. */
    @Transactional
    public void dismiss(@NonNull Long reportId) {
        ContentReport report = findReport(reportId);
        contentReportRepository.resolveByTarget(report.getTargetType(), report.getTargetId());
    }

    /** Deletes the reported watch list, item or account and closes every report about it. */
    @Transactional
    public void removeTarget(@NonNull Long reportId) {
        ContentReport report = findReport(reportId);
        Long targetId = report.getTargetId();
        switch (report.getTargetType()) {
            case WATCH_LIST -> {
                if (watchListRepository.existsById(targetId)) watchListService.deleteWatchListAsAdmin(targetId);
            }
            case ITEM -> itemRepository.findById(targetId).ifPresent(item -> {
                Long creatorId = item.getCreator().getId();
                String image = item.getUrlAvatar();
                itemService.deleteItemAsAdmin(item);
                deleteImage(image, creatorId);
            });
            case USER -> userRepository.findById(targetId).ifPresent(user ->
                    userService.delete(user).forEach(image -> deleteImage(image, targetId)));
        }
        contentReportRepository.resolveByTarget(report.getTargetType(), targetId);
    }

    private ContentReport findReport(Long reportId) {
        return contentReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
    }

    private Long findOwnerId(ReportTargetType targetType, Long targetId) {
        return switch (targetType) {
            case WATCH_LIST -> watchListRepository.findById(targetId)
                    .map(watchList -> watchList.getCreator().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Watch list not found"));
            case ITEM -> itemRepository.findById(targetId)
                    .map(item -> item.getCreator().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
            case USER -> userRepository.findById(targetId)
                    .map(User::getId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        };
    }

    private String findTargetName(ContentReport report) {
        Long targetId = report.getTargetId();
        return switch (report.getTargetType()) {
            case WATCH_LIST -> watchListRepository.findById(targetId).map(WatchList::getName).orElse(null);
            case ITEM -> itemRepository.findById(targetId).map(Item::getName).orElse(null);
            case USER -> userRepository.findById(targetId).map(User::getNickname).orElse(null);
        };
    }

    // The content is already gone, so a storage failure must not turn this into an error
    private void deleteImage(String url, Long ownerId) {
        try {
            imageStorageService.deleteIfOwned(url, ownerId);
        } catch (RuntimeException e) {
            log.warn("Could not delete image of user {}: {}", ownerId, e.getMessage());
        }
    }

}
