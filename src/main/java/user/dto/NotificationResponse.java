package user.dto;

import java.time.LocalDateTime;

public class NotificationResponse {
    private Long id;
    private String type;
    private String message;
    private String relatedUser;
    private Long relatedArticleId;
    private String relatedArticleTitle;
    private Long relatedCommentId;
    private boolean read;
    private LocalDateTime createdAt;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getRelatedUser() { return relatedUser; }
    public void setRelatedUser(String relatedUser) { this.relatedUser = relatedUser; }
    public Long getRelatedArticleId() { return relatedArticleId; }
    public void setRelatedArticleId(Long relatedArticleId) { this.relatedArticleId = relatedArticleId; }
    public String getRelatedArticleTitle() { return relatedArticleTitle; }
    public void setRelatedArticleTitle(String relatedArticleTitle) { this.relatedArticleTitle = relatedArticleTitle; }
    public Long getRelatedCommentId() { return relatedCommentId; }
    public void setRelatedCommentId(Long relatedCommentId) { this.relatedCommentId = relatedCommentId; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
} 