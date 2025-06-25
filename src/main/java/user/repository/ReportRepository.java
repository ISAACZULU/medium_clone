package user.repository;

import user.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByStatusOrderByCreatedAtDesc(Report.ReportStatus status);
    List<Report> findByContentTypeAndStatusOrderByCreatedAtDesc(Report.ContentType contentType, Report.ReportStatus status);
    List<Report> findByReporterIdOrderByCreatedAtDesc(Long reporterId);
    List<Report> findByReportedContentIdAndContentType(Long reportedContentId, Report.ContentType contentType);
    long countByStatus(Report.ReportStatus status);
    long countByContentType(Report.ContentType contentType);
} 