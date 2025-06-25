package user.repository;

import com.medium_clone.user.entity.BookmarkCollection;
import com.medium_clone.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookmarkCollectionRepository extends JpaRepository<BookmarkCollection, Long> {
    List<BookmarkCollection> findByUser(User user);
    List<BookmarkCollection> findByUserAndIsPublicTrue(User user);
    List<BookmarkCollection> findByIsPublicTrue();
} 