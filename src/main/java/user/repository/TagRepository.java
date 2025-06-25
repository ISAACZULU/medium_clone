package user.repository;

import com.medium_clone.user.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByNameIgnoreCase(String name);

    // Auto-complete: find tags starting with query
    List<Tag> findTop10ByNameStartingWithIgnoreCaseOrderByUsageCountDesc(String prefix);

    // Trending tags: top N by usage count or recent usage
    @Query("SELECT t FROM Tag t ORDER BY t.usageCount DESC, t.lastUsedAt DESC")
    List<Tag> findTrendingTags(org.springframework.data.domain.Pageable pageable);
} 