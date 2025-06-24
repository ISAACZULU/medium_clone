package user.repository;

import com.medium_clone.user.entity.ArticleVersion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleVersionRepository extends JpaRepository<ArticleVersion, Long> {
    
    List<ArticleVersion> findByArticleIdOrderByVersionNumberDesc(Long articleId);
    
    Optional<ArticleVersion> findByArticleIdAndVersionNumber(Long articleId, Integer versionNumber);
    
    Page<ArticleVersion> findByArticleId(Long articleId, Pageable pageable);
    
    Integer countByArticleId(Long articleId);
    
    void deleteByArticleId(Long articleId);
} 