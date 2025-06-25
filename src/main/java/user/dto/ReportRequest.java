package user.dto;

import user.entity.Report;

public class ReportRequest {
    private Report.ContentType contentType;
    private Long reportedContentId;
    private String reason;

    // Getters and setters
    public Report.ContentType getContentType() { return contentType; }
    public void setContentType(Report.ContentType contentType) { this.contentType = contentType; }
    public Long getReportedContentId() { return reportedContentId; }
    public void setReportedContentId(Long reportedContentId) { this.reportedContentId = reportedContentId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
} 