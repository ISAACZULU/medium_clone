package user.repository;

import com.medium_clone.user.entity.ArticleEngagement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleEngagementRepository extends JpaRepository<ArticleEngagement, Long> {
    
    // Check if user has engaged with article
    Optional<ArticleEngagement> findByArticleIdAndUserIdAndType(Long articleId, Long userId, ArticleEngagement.EngagementType type);
    
    // Count engagements by type for an article
    Long countByArticleIdAndType(Long articleId, ArticleEngagement.EngagementType type);
    
    // Get user's engagements
    List<ArticleEngagement> findByUserId(Long userId);
    
    // Get article's engagements
    List<ArticleEngagement> findByArticleId(Long articleId);
    
    // Get trending articles (high engagement in recent time)
    @Query("SELECT ae.article.id, COUNT(ae) as engagementCount " +
           "FROM ArticleEngagement ae " +
           "WHERE ae.createdAt >= :since " +
           "GROUP BY ae.article.id " +
           "ORDER BY engagementCount DESC")
    Page<Object[]> findTrendingArticles(@Param("since") LocalDateTime since, Pageable pageable);
    
    // Get articles by followed authors
    @Query("SELECT DISTINCT a FROM Article a " +
           "JOIN a.author u " +
           "JOIN u.followers f " +
           "WHERE f.id = :userId AND a.published = true " +
           "ORDER BY a.publishedAt DESC")
    Page<Object[]> findArticlesByFollowedAuthors(@Param("userId") Long userId, Pageable pageable);
    
    // Get articles with followed tags
    @Query("SELECT DISTINCT a FROM Article a " +
           "JOIN a.tags t " +
           "JOIN User u " +
           "JOIN u.following f " +
           "WHERE f.username IN (SELECT u2.username FROM User u2 WHERE u2.id = :userId) " +
           "AND t IN (SELECT t2 FROM User u3 JOIN u3.following f2 WHERE f2.id = :userId) " +
           "AND a.published = true " +
           "ORDER BY a.publishedAt DESC")
    Page<Object[]> findArticlesByFollowedTags(@Param("userId") Long userId, Pageable pageable);

    // Find claps by user for an article
    Optional<ArticleEngagement> findByArticleIdAndUserIdAndType(Long articleId, Long userId, ArticleEngagement.EngagementType type);

    // Sum all claps for an article
    @Query("SELECT COALESCE(SUM(ae.count), 0) FROM ArticleEngagement ae WHERE ae.article.id = :articleId AND ae.type = 'CLAP'")
    Long sumClapsByArticleId(@Param("articleId") Long articleId);
} 