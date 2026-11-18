package com.topwatch.back_topwatch.dto;

import com.topwatch.back_topwatch.domain.enums.ReportReason;
import com.topwatch.back_topwatch.domain.enums.ReportTargetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReportRequest(

        @NotNull(message = "Target type is required")
        ReportTargetType targetType,

        @NotNull(message = "Target id is required")
        Long targetId,

        @NotNull(message = "Reason is required")
        ReportReason reason,

        @Size(max = 500, message = "Details must be 500 characters or less")
        String details

) {}
