package com.topwatch.back_topwatch.service;

import com.topwatch.back_topwatch.domain.ContentReport;
import com.topwatch.back_topwatch.domain.Item;
import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.domain.WatchList;
import com.topwatch.back_topwatch.domain.enums.ReportReason;
import com.topwatch.back_topwatch.domain.enums.ReportStatus;
import com.topwatch.back_topwatch.domain.enums.ReportTargetType;
import com.topwatch.back_topwatch.dto.CreateReportRequest;
import com.topwatch.back_topwatch.exception.BadRequestException;
import com.topwatch.back_topwatch.exception.ResourceNotFoundException;
import com.topwatch.back_topwatch.repository.ContentReportRepository;
import com.topwatch.back_topwatch.repository.ItemRepository;
import com.topwatch.back_topwatch.repository.UserRepository;
import com.topwatch.back_topwatch.repository.WatchListRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private ContentReportRepository contentReportRepository;
    @Mock private WatchListRepository watchListRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private UserRepository userRepository;
    @Mock private WatchListService watchListService;
    @Mock private ItemService itemService;
    @Mock private UserService userService;
    @Mock private ImageStorageService imageStorageService;

    @InjectMocks
    private ReportService reportService;

    private final User reporter = User.builder().id(1L).nickname("sebas").build();
    private final User owner = User.builder().id(2L).nickname("otro").build();

    private CreateReportRequest listReport() {
        return new CreateReportRequest(ReportTargetType.WATCH_LIST, 5L, ReportReason.SPAM, "  spam  ");
    }

    @Test
    void create_savesOpenReport() {
        when(watchListRepository.findById(5L)).thenReturn(Optional.of(WatchList.builder().id(5L).creator(owner).build()));

        reportService.create(listReport(), reporter);

        ArgumentCaptor<ContentReport> saved = ArgumentCaptor.forClass(ContentReport.class);
        verify(contentReportRepository).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(ReportStatus.OPEN);
        assertThat(saved.getValue().getDetails()).isEqualTo("spam");
    }

    @Test
    void create_rejectsOwnContent() {
        when(watchListRepository.findById(5L)).thenReturn(Optional.of(WatchList.builder().id(5L).creator(reporter).build()));

        assertThatThrownBy(() -> reportService.create(listReport(), reporter)).isInstanceOf(BadRequestException.class);
        verify(contentReportRepository, never()).save(any());
    }

    @Test
    void create_failsWhenTargetDoesNotExist() {
        when(watchListRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.create(listReport(), reporter)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_ignoresRepeatedOpenReport() {
        when(watchListRepository.findById(5L)).thenReturn(Optional.of(WatchList.builder().id(5L).creator(owner).build()));
        when(contentReportRepository.existsByReporter_IdAndTargetTypeAndTargetIdAndStatus(
                1L, ReportTargetType.WATCH_LIST, 5L, ReportStatus.OPEN)).thenReturn(true);

        reportService.create(listReport(), reporter);

        verify(contentReportRepository, never()).save(any());
    }

    @Test
    void removeTarget_deletesItemAndItsImageAndClosesReports() {
        Item item = Item.builder().id(9L).creator(owner).urlAvatar("cover.png").build();
        ContentReport report = ContentReport.builder().id(3L).targetType(ReportTargetType.ITEM).targetId(9L).build();
        when(contentReportRepository.findById(3L)).thenReturn(Optional.of(report));
        when(itemRepository.findById(9L)).thenReturn(Optional.of(item));

        reportService.removeTarget(3L);

        verify(itemService).deleteItemAsAdmin(item);
        verify(imageStorageService).deleteIfOwned("cover.png", 2L);
        verify(contentReportRepository).resolveByTarget(ReportTargetType.ITEM, 9L);
    }

    @Test
    void dismiss_closesReportsWithoutTouchingContent() {
        ContentReport report = ContentReport.builder().id(3L).targetType(ReportTargetType.WATCH_LIST).targetId(5L).build();
        when(contentReportRepository.findById(3L)).thenReturn(Optional.of(report));

        reportService.dismiss(3L);

        verify(contentReportRepository).resolveByTarget(ReportTargetType.WATCH_LIST, 5L);
        verify(watchListService, never()).deleteWatchListAsAdmin(any());
    }

}
