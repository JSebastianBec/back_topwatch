package com.topwatch.back_topwatch.repository;

import com.topwatch.back_topwatch.domain.ContentReport;
import com.topwatch.back_topwatch.domain.enums.ReportStatus;
import com.topwatch.back_topwatch.domain.enums.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentReportRepository extends JpaRepository<ContentReport, Long> {

    List<ContentReport> findByStatusOrderByCreationDateAsc(ReportStatus status);

    boolean existsByReporter_IdAndTargetTypeAndTargetIdAndStatus(
            Long reporterId, ReportTargetType targetType, Long targetId, ReportStatus status);

    /** Closes every open report about the same target once it has been dealt with. */
    @Modifying
    @Query("""
            update ContentReport r set r.status = com.topwatch.back_topwatch.domain.enums.ReportStatus.RESOLVED
            where r.targetType = :targetType and r.targetId = :targetId
            """)
    void resolveByTarget(@Param("targetType") ReportTargetType targetType, @Param("targetId") Long targetId);

    @Modifying
    @Query("delete from ContentReport r where r.reporter.id = :reporterId")
    void deleteByReporterId(@Param("reporterId") Long reporterId);

}
