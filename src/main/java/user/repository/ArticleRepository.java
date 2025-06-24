package user.repository;

import com.medium_clone.user.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    
    Optional<Article> findBySlug(String slug);
    
    Page<Article> findByAuthorUsername(String username, Pageable pageable);
    
    Page<Article> findByPublishedTrue(Pageable pageable);
    
    @Query("SELECT a FROM Article a WHERE a.published = true AND :tag MEMBER OF a.tags")
    Page<Article> findByTag(@Param("tag") String tag, Pageable pageable);
    
    @Query("SELECT a FROM Article a WHERE a.published = true AND " +
           "LOWER(a.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.content) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Article> searchArticles(@Param("search") String search, Pageable pageable);
    
    List<Article> findByAuthorIdAndPublishedTrue(Long authorId);
} 