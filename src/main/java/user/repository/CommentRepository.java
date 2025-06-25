package user.repository;

import com.medium_clone.user.entity.Comment;
import com.medium_clone.user.entity.Article;
import com.medium_clone.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByArticleAndParentIsNullOrderByCreatedAtAsc(Article article);
    List<Comment> findByParent(Comment parent);
    List<Comment> findByUser(User user);
    List<Comment> findByArticle(Article article);
} 