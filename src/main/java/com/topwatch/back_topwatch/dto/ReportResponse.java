package com.topwatch.back_topwatch.dto;

import com.topwatch.back_topwatch.domain.ContentReport;
import com.topwatch.back_topwatch.domain.enums.ReportReason;
import com.topwatch.back_topwatch.domain.enums.ReportTargetType;

import java.time.Instant;

/** targetName is null when the reported content no longer exists. */
public record ReportResponse(
        Long id,
        String reporterNickname,
        ReportTargetType targetType,
        Long targetId,
        String targetName,
        ReportReason reason,
        String details,
        Instant creationDate
) {

    public static ReportResponse from(ContentReport report, String targetName) {
        return new ReportResponse(
                report.getId(),
                report.getReporter().getNickname(),
                report.getTargetType(),
                report.getTargetId(),
                targetName,
                report.getReason(),
                report.getDetails(),
                report.getCreationDate()
        );
    }

}
