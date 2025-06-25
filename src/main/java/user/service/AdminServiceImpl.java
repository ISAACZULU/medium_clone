package user.service;

import user.entity.User;
import user.entity.Comment;
import user.entity.Article;
import user.repository.UserRepository;
import user.repository.CommentRepository;
import user.repository.ArticleRepository;
import user.repository.ArticleEngagementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final ArticleEngagementRepository engagementRepository;

    @Autowired
    public AdminServiceImpl(UserRepository userRepository, CommentRepository commentRepository, 
                          ArticleRepository articleRepository, ArticleEngagementRepository engagementRepository) {
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
        this.engagementRepository = engagementRepository;
    }

    // User Management
    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Override
    @Transactional
    public User updateUserRole(Long userId, String role) {
        User user = getUserById(userId);
        user.setRole(User.UserRole.valueOf(role.toUpperCase()));
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deactivateUser(Long userId) {
        User user = getUserById(userId);
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void activateUser(Long userId) {
        User user = getUserById(userId);
        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }

    // Content Moderation
    @Override
    @Transactional(readOnly = true)
    public List<Object> getFlaggedComments() {
        List<Comment> flaggedComments = commentRepository.findByFlaggedTrue();
        return flaggedComments.stream()
                .map(this::commentToMap)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void approveComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        comment.setFlagged(false);
        comment.setFlagReason(null);
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void rejectComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object> getFlaggedArticles() {
        // For now, return empty list as we don't have article flagging implemented
        // This would be implemented when article flagging is added
        return List.of();
    }

    @Override
    @Transactional
    public void approveArticle(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));
        // Add article approval logic here
        articleRepository.save(article);
    }

    @Override
    @Transactional
    public void rejectArticle(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));
        article.setPublished(false);
        articleRepository.save(article);
    }

    @Override
    @Transactional
    public void banUser(Long userId, String reason) {
        User user = getUserById(userId);
        user.setActive(false);
        // Could add a ban reason field to User entity
        userRepository.save(user);
    }

    // Analytics
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPlatformAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActiveTrue();
        long totalArticles = articleRepository.count();
        long publishedArticles = articleRepository.countByPublishedTrue();
        
        analytics.put("totalUsers", totalUsers);
        analytics.put("activeUsers", activeUsers);
        analytics.put("inactiveUsers", totalUsers - activeUsers);
        analytics.put("totalArticles", totalArticles);
        analytics.put("publishedArticles", publishedArticles);
        analytics.put("draftArticles", totalArticles - publishedArticles);
        analytics.put("userActivationRate", totalUsers > 0 ? (double) activeUsers / totalUsers : 0.0);
        analytics.put("articlePublishRate", totalArticles > 0 ? (double) publishedArticles / totalArticles : 0.0);
        
        return analytics;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        long totalUsers = userRepository.count();
        long newUsersThisWeek = userRepository.countByCreatedAtAfter(LocalDateTime.now().minusWeeks(1));
        long newUsersThisMonth = userRepository.countByCreatedAtAfter(LocalDateTime.now().minusMonths(1));
        
        analytics.put("totalUsers", totalUsers);
        analytics.put("newUsersThisWeek", newUsersThisWeek);
        analytics.put("newUsersThisMonth", newUsersThisMonth);
        analytics.put("weeklyGrowthRate", totalUsers > 0 ? (double) newUsersThisWeek / totalUsers : 0.0);
        analytics.put("monthlyGrowthRate", totalUsers > 0 ? (double) newUsersThisMonth / totalUsers : 0.0);
        
        return analytics;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getContentAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        long totalArticles = articleRepository.count();
        long publishedArticles = articleRepository.countByPublishedTrue();
        long articlesThisWeek = articleRepository.countByCreatedAtAfter(LocalDateTime.now().minusWeeks(1));
        long articlesThisMonth = articleRepository.countByCreatedAtAfter(LocalDateTime.now().minusMonths(1));
        
        analytics.put("totalArticles", totalArticles);
        analytics.put("publishedArticles", publishedArticles);
        analytics.put("articlesThisWeek", articlesThisWeek);
        analytics.put("articlesThisMonth", articlesThisMonth);
        analytics.put("averageArticlesPerWeek", articlesThisWeek);
        analytics.put("averageArticlesPerMonth", articlesThisMonth);
        
        return analytics;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getEngagementAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        long totalViews = engagementRepository.countByType(ArticleEngagement.EngagementType.VIEW);
        long totalClaps = engagementRepository.countByType(ArticleEngagement.EngagementType.CLAP);
        long totalComments = engagementRepository.countByType(ArticleEngagement.EngagementType.COMMENT);
        long totalBookmarks = engagementRepository.countByType(ArticleEngagement.EngagementType.BOOKMARK);
        
        analytics.put("totalViews", totalViews);
        analytics.put("totalClaps", totalClaps);
        analytics.put("totalComments", totalComments);
        analytics.put("totalBookmarks", totalBookmarks);
        analytics.put("averageViewsPerArticle", articleRepository.countByPublishedTrue() > 0 ? 
                     (double) totalViews / articleRepository.countByPublishedTrue() : 0.0);
        analytics.put("averageClapsPerArticle", articleRepository.countByPublishedTrue() > 0 ? 
                     (double) totalClaps / articleRepository.countByPublishedTrue() : 0.0);
        
        return analytics;
    }

    private Map<String, Object> commentToMap(Comment comment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", comment.getId());
        map.put("content", comment.getContent());
        map.put("user", comment.getUser().getUsername());
        map.put("article", comment.getArticle().getTitle());
        map.put("flagReason", comment.getFlagReason());
        map.put("createdAt", comment.getCreatedAt());
        return map;
    }
} 