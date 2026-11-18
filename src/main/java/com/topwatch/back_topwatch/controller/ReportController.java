package com.topwatch.back_topwatch.controller;

import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.dto.CreateReportRequest;
import com.topwatch.back_topwatch.dto.ReportResponse;
import com.topwatch.back_topwatch.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Reports of objectionable content and their review")
public class ReportController {

    private final ReportService reportService;

    @Operation(
            summary = "Report content",
            description = "Reports a watch list, an item or a user for an admin to review",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/api/v1/reports")
    public ResponseEntity<Void> createReport(
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        reportService.create(request, currentUser);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List open reports", description = "Admin only", security = @SecurityRequirement(name = "Bearer Token"))
    @GetMapping("/api/v1/admin/reports")
    public List<ReportResponse> getOpenReports() {
        return reportService.findOpenReports();
    }

    @Operation(summary = "Dismiss report", description = "Admin only. Keeps the content", security = @SecurityRequirement(name = "Bearer Token"))
    @PostMapping("/api/v1/admin/reports/{id}/dismiss")
    public ResponseEntity<Void> dismissReport(@PathVariable Long id) {
        reportService.dismiss(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Remove reported content",
            description = "Admin only. Deletes the reported watch list, item or account",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/api/v1/admin/reports/{id}/remove-content")
    public ResponseEntity<Void> removeReportedContent(@PathVariable Long id) {
        reportService.removeTarget(id);
        return ResponseEntity.noContent().build();
    }

}
