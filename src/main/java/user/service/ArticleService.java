package user.service;

import com.medium_clone.user.dto.CreateArticleRequest;
import com.medium_clone.user.dto.ArticleResponse;
import com.medium_clone.user.dto.ArticleVersionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ArticleService {
    
    ArticleResponse createArticle(String authorEmail, CreateArticleRequest request);
    
    ArticleResponse updateArticle(String authorEmail, Long articleId, CreateArticleRequest request);
    
    ArticleResponse getArticleBySlug(String slug);
    
    ArticleResponse getArticleById(Long id);
    
    Page<ArticleResponse> getArticlesByAuthor(String username, Pageable pageable);
    
    Page<ArticleResponse> getPublishedArticles(Pageable pageable);
    
    Page<ArticleResponse> getArticlesByTag(String tag, Pageable pageable);
    
    Page<ArticleResponse> searchArticles(String search, Pageable pageable);
    
    void deleteArticle(String authorEmail, Long articleId);
    
    void publishArticle(String authorEmail, Long articleId);
    
    void unpublishArticle(String authorEmail, Long articleId);
    
    // Versioning methods
    List<ArticleVersionResponse> getArticleVersions(Long articleId);
    
    ArticleVersionResponse getArticleVersion(Long articleId, Integer versionNumber);
    
    ArticleResponse restoreArticleVersion(String authorEmail, Long articleId, Integer versionNumber);
    
    void deleteArticleVersion(String authorEmail, Long articleId, Integer versionNumber);
} 