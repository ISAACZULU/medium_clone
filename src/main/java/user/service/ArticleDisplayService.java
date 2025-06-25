package user.service;

import com.medium_clone.user.dto.ArticleDisplayResponse;
import com.medium_clone.user.dto.ArticleResponse;

import java.util.Map;

public interface ArticleDisplayService {
    
    /**
     * Get enhanced article display with analytics
     */
    ArticleDisplayResponse getArticleForDisplay(String slug, String userEmail);
    
    /**
     * Get article preview without tracking
     */
    ArticleDisplayResponse getArticlePreview(String slug);
    
    /**
     * Get article statistics and engagement metrics
     */
    Map<String, Object> getArticleStats(String slug);
    
    /**
     * Track reading progress
     */
    Map<String, Object> trackReadingProgress(String slug, String userEmail, int scrollPercentage);
    
    /**
     * Get related articles
     */
    Map<String, Object> getRelatedArticles(String slug, int limit);
    
    /**
     * Calculate content quality score
     */
    double calculateContentQualityScore(String content);
    
    /**
     * Generate SEO metadata
     */
    Map<String, String> generateSeoMetadata(ArticleResponse article);
    
    /**
     * Analyze content structure
     */
    Map<String, Object> analyzeContentStructure(String content);
    
    /**
     * Get reading level assessment
     */
    String assessReadingLevel(String content);
    
    /**
     * Calculate engagement metrics
     */
    Map<String, Object> calculateEngagementMetrics(Long articleId);
    
    /**
     * Get article performance insights
     */
    Map<String, Object> getPerformanceInsights(String slug);
    
    /**
     * Generate social sharing data
     */
    Map<String, String> generateSocialSharingData(ArticleResponse article);
    
    /**
     * Validate and suggest improvements for article
     */
    Map<String, Object> validateArticle(String slug);
} 