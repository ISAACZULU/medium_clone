package user.service;

import com.medium_clone.user.dto.ArticleResponse;
import com.medium_clone.user.dto.ArticleSearchRequest;
import com.medium_clone.user.dto.PersonalizedFeedRequest;
import org.springframework.data.domain.Page;

public interface ArticleDiscoveryService {
    
    // Personalized feed based on user's following and preferences
    Page<ArticleResponse> getPersonalizedFeed(String userEmail, PersonalizedFeedRequest request);
    
    // Trending articles by engagement
    Page<ArticleResponse> getTrendingArticles(int page, int size);
    
    // Advanced search with multiple filters
    Page<ArticleResponse> advancedSearch(ArticleSearchRequest request);
    
    // Search by multiple tags
    Page<ArticleResponse> searchByTags(java.util.Set<String> tags, int page, int size);
    
    // Recent articles
    Page<ArticleResponse> getRecentArticles(int page, int size);
    
    // Track article engagement (view, like, bookmark, share)
    void trackEngagement(String userEmail, Long articleId, String engagementType);
    
    // Get engagement statistics for an article
    java.util.Map<String, Long> getArticleEngagementStats(Long articleId);
} 