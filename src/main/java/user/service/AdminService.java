package user.service;

import user.entity.User;
import java.util.List;
import java.util.Map;

public interface AdminService {
    // User Management
    List<User> getAllUsers();
    User getUserById(Long userId);
    User updateUserRole(Long userId, String role);
    void deactivateUser(Long userId);
    void activateUser(Long userId);
    void deleteUser(Long userId);
    
    // Content Moderation
    List<Object> getFlaggedComments();
    void approveComment(Long commentId);
    void rejectComment(Long commentId);
    List<Object> getFlaggedArticles();
    void approveArticle(Long articleId);
    void rejectArticle(Long articleId);
    void banUser(Long userId, String reason);
    
    // Analytics
    Map<String, Object> getPlatformAnalytics();
    Map<String, Object> getUserAnalytics();
    Map<String, Object> getContentAnalytics();
    Map<String, Object> getEngagementAnalytics();
} 