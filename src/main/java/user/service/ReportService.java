package user.service;

import user.dto.ReportRequest;
import user.dto.ReportResponse;
import user.entity.Report;
import java.util.List;

public interface ReportService {
    // User reporting
    ReportResponse submitReport(String reporterEmail, ReportRequest request);
    List<ReportResponse> getUserReports(String userEmail);
    
    // Moderator operations
    List<ReportResponse> getPendingReports();
    List<ReportResponse> getReportsByStatus(Report.ReportStatus status);
    List<ReportResponse> getReportsByContentType(Report.ContentType contentType);
    ReportResponse updateReportStatus(Long reportId, Report.ReportStatus status, String moderatorEmail, String notes);
    ReportResponse assignReportToModerator(Long reportId, String moderatorEmail);
    
    // Admin operations
    void dismissReport(Long reportId, String moderatorEmail, String reason);
    void resolveReport(Long reportId, String moderatorEmail, String action);
} 