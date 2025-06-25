package user.service;

import com.medium_clone.user.dto.CommentRequest;
import com.medium_clone.user.dto.CommentResponse;
import com.medium_clone.user.entity.Article;
import com.medium_clone.user.entity.Comment;
import com.medium_clone.user.entity.User;
import user.repository.ArticleRepository;
import user.repository.CommentRepository;
import user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, ArticleRepository articleRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public CommentResponse addComment(Long articleId, String userEmail, CommentRequest request) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
        }
        Comment comment = Comment.builder()
                .article(article)
                .user(user)
                .parent(parent)
                .content(request.getContent())
                .deleted(false)
                .flagged(false)
                .build();
        Comment saved = commentRepository.save(comment);
        return toResponse(saved, true);
    }

    @Override
    @Transactional
    public CommentResponse editComment(Long commentId, String userEmail, String newContent) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (!comment.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You can only edit your own comments");
        }
        if (comment.isDeleted()) {
            throw new IllegalArgumentException("Cannot edit a deleted comment");
        }
        comment.setContent(newContent);
        Comment saved = commentRepository.save(comment);
        return toResponse(saved, true);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, String userEmail) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (!comment.getUser().getEmail().equals(userEmail)) {
            throw new IllegalArgumentException("You can only delete your own comments");
        }
        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void flagComment(Long commentId, String userEmail, String reason) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        comment.setFlagged(true);
        comment.setFlagReason(reason);
        commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsForArticle(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));
        List<Comment> roots = commentRepository.findByArticleAndParentIsNullOrderByCreatedAtAsc(article);
        return roots.stream().map(c -> toResponse(c, true)).collect(Collectors.toList());
    }

    private CommentResponse toResponse(Comment comment, boolean includeReplies) {
        List<CommentResponse> replies = new ArrayList<>();
        if (includeReplies && comment.getChildren() != null) {
            replies = comment.getChildren().stream()
                    .map(child -> toResponse(child, true))
                    .collect(Collectors.toList());
        }
        return CommentResponse.builder()
                .id(comment.getId())
                .articleId(comment.getArticle().getId())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .userImage(comment.getUser().getImage())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .content(comment.isDeleted() ? null : comment.getContent())
                .deleted(comment.isDeleted())
                .flagged(comment.isFlagged())
                .flagReason(comment.getFlagReason())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .replies(replies)
                .build();
    }
} 