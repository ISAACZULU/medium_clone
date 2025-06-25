package user.repository;

import com.medium_clone.user.entity.Bookmark;
import com.medium_clone.user.entity.Article;
import com.medium_clone.user.entity.User;
import com.medium_clone.user.entity.BookmarkCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByUser(User user);
    List<Bookmark> findByUserAndCollection(User user, BookmarkCollection collection);
    List<Bookmark> findByCollection(BookmarkCollection collection);
    Optional<Bookmark> findByUserAndArticleAndCollection(User user, Article article, BookmarkCollection collection);
    List<Bookmark> findByArticle(Article article);
} 