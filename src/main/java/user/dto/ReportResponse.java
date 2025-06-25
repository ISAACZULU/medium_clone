package user.dto;

import user.entity.Report;
import java.time.LocalDateTime;

public class ReportResponse {
    private Long id;
    private String reporterUsername;
    private String contentType;
    private Long reportedContentId;
    private String reportedContentTitle; // Article title or comment preview
    private String reason;
    private String status;
    private String moderatorNotes;
    private String moderatorUsername;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReporterUsername() { return reporterUsername; }
    public void setReporterUsername(String reporterUsername) { this.reporterUsername = reporterUsername; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getReportedContentId() { return reportedContentId; }
    public void setReportedContentId(Long reportedContentId) { this.reportedContentId = reportedContentId; }
    public String getReportedContentTitle() { return reportedContentTitle; }
    public void setReportedContentTitle(String reportedContentTitle) { this.reportedContentTitle = reportedContentTitle; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getModeratorNotes() { return moderatorNotes; }
    public void setModeratorNotes(String moderatorNotes) { this.moderatorNotes = moderatorNotes; }
    public String getModeratorUsername() { return moderatorUsername; }
    public void setModeratorUsername(String moderatorUsername) { this.moderatorUsername = moderatorUsername; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
} 