package user.dto;

public class NotificationPreferencesRequest {
    private boolean receiveFollowNotifications = true;
    private boolean receiveClapNotifications = true;
    private boolean receiveCommentNotifications = true;
    private boolean receiveMentionNotifications = true;
    private boolean receiveRecommendationNotifications = true;
    private boolean emailNotificationsEnabled = true;
    private boolean pushNotificationsEnabled = true;
    private String emailDigestFrequency = "DAILY";

    // Getters and setters
    public boolean isReceiveFollowNotifications() { return receiveFollowNotifications; }
    public void setReceiveFollowNotifications(boolean receiveFollowNotifications) { this.receiveFollowNotifications = receiveFollowNotifications; }
    public boolean isReceiveClapNotifications() { return receiveClapNotifications; }
    public void setReceiveClapNotifications(boolean receiveClapNotifications) { this.receiveClapNotifications = receiveClapNotifications; }
    public boolean isReceiveCommentNotifications() { return receiveCommentNotifications; }
    public void setReceiveCommentNotifications(boolean receiveCommentNotifications) { this.receiveCommentNotifications = receiveCommentNotifications; }
    public boolean isReceiveMentionNotifications() { return receiveMentionNotifications; }
    public void setReceiveMentionNotifications(boolean receiveMentionNotifications) { this.receiveMentionNotifications = receiveMentionNotifications; }
    public boolean isReceiveRecommendationNotifications() { return receiveRecommendationNotifications; }
    public void setReceiveRecommendationNotifications(boolean receiveRecommendationNotifications) { this.receiveRecommendationNotifications = receiveRecommendationNotifications; }
    public boolean isEmailNotificationsEnabled() { return emailNotificationsEnabled; }
    public void setEmailNotificationsEnabled(boolean emailNotificationsEnabled) { this.emailNotificationsEnabled = emailNotificationsEnabled; }
    public boolean isPushNotificationsEnabled() { return pushNotificationsEnabled; }
    public void setPushNotificationsEnabled(boolean pushNotificationsEnabled) { this.pushNotificationsEnabled = pushNotificationsEnabled; }
    public String getEmailDigestFrequency() { return emailDigestFrequency; }
    public void setEmailDigestFrequency(String emailDigestFrequency) { this.emailDigestFrequency = emailDigestFrequency; }
} 