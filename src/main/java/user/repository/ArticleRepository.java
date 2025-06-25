package user.repository;

import com.medium_clone.user.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    
    Optional<Article> findBySlug(String slug);
    
    Page<Article> findByAuthorUsername(String username, Pageable pageable);
    
    Page<Article> findByAuthorUsernameIn(List<String> usernames, Pageable pageable);
    
    Page<Article> findByPublishedTrue(Pageable pageable);
    
    @Query("SELECT a FROM Article a WHERE a.published = true AND :tag MEMBER OF a.tags")
    Page<Article> findByTag(@Param("tag") String tag, Pageable pageable);
    
    @Query("SELECT a FROM Article a WHERE a.published = true AND " +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.content) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Article> searchArticles(@Param("search") String search, Pageable pageable);
    
    List<Article> findByAuthorIdAndPublishedTrue(Long authorId);
    
    // Advanced search with multiple filters
    @Query("SELECT a FROM Article a " +
           "WHERE (:keywords IS NULL OR " +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :keywords, '%')) OR " +
           "LOWER(a.content) LIKE LOWER(CONCAT('%', :keywords, '%'))) " +
           "AND (:authorUsername IS NULL OR a.author.username = :authorUsername) " +
           "AND (:fromDate IS NULL OR a.publishedAt >= :fromDate) " +
           "AND (:toDate IS NULL OR a.publishedAt <= :toDate) " +
           "AND (:publishedOnly = false OR a.published = true)")
    Page<Article> advancedSearch(@Param("keywords") String keywords,
                                @Param("authorUsername") String authorUsername,
                                @Param("fromDate") LocalDateTime fromDate,
                                @Param("toDate") LocalDateTime toDate,
                                @Param("publishedOnly") boolean publishedOnly,
                                Pageable pageable);
    
    // Search by multiple tags
    @Query("SELECT a FROM Article a " +
           "JOIN a.tags t " +
           "WHERE t IN :tags AND a.published = true " +
           "GROUP BY a " +
           "HAVING COUNT(DISTINCT t) = :tagCount")
    Page<Article> findByMultipleTags(@Param("tags") Set<String> tags,
                                    @Param("tagCount") Long tagCount,
                                    Pageable pageable);
    
    // Get trending articles by view count
    @Query("SELECT a FROM Article a " +
           "WHERE a.published = true " +
           "ORDER BY a.viewCount DESC, a.publishedAt DESC")
    Page<Article> findTrendingByViews(Pageable pageable);
    
    // Get recent articles
    @Query("SELECT a FROM Article a " +
           "WHERE a.published = true " +
           "ORDER BY a.publishedAt DESC")
    Page<Article> findRecentArticles(Pageable pageable);
} 