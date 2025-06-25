package user.service;

import com.medium_clone.user.dto.CommentRequest;
import com.medium_clone.user.dto.CommentResponse;

import java.util.List;

public interface CommentService {
    CommentResponse addComment(Long articleId, String userEmail, CommentRequest request);
    CommentResponse editComment(Long commentId, String userEmail, String newContent);
    void deleteComment(Long commentId, String userEmail);
    void flagComment(Long commentId, String userEmail, String reason);
    List<CommentResponse> getCommentsForArticle(Long articleId);
} 