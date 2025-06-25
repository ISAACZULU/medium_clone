package user.controller;

import com.medium_clone.user.dto.CommentRequest;
import com.medium_clone.user.dto.CommentResponse;
import user.service.CommentService;
import user.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentService commentService;
    private final JwtUtil jwtUtil;

    @Autowired
    public CommentController(CommentService commentService, JwtUtil jwtUtil) {
        this.commentService = commentService;
        this.jwtUtil = jwtUtil;
    }

    // Add comment or reply
    @PostMapping("/article/{articleId}")
    public CommentResponse addComment(@PathVariable Long articleId,
                                      @Valid @RequestBody CommentRequest request,
                                      @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return commentService.addComment(articleId, email, request);
    }

    // Edit comment
    @PutMapping("/{commentId}")
    public CommentResponse editComment(@PathVariable Long commentId,
                                       @RequestBody Map<String, String> body,
                                       @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        String newContent = body.get("content");
        return commentService.editComment(commentId, email, newContent);
    }

    // Delete comment (soft delete)
    @DeleteMapping("/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId,
                                                @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        commentService.deleteComment(commentId, email);
        return ResponseEntity.ok("Comment deleted");
    }

    // Flag comment
    @PostMapping("/{commentId}/flag")
    public ResponseEntity<String> flagComment(@PathVariable Long commentId,
                                              @RequestBody Map<String, String> body,
                                              @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        String reason = body.getOrDefault("reason", "Inappropriate");
        commentService.flagComment(commentId, email, reason);
        return ResponseEntity.ok("Comment flagged");
    }

    // Get all comments for an article (nested)
    @GetMapping("/article/{articleId}")
    public List<CommentResponse> getCommentsForArticle(@PathVariable Long articleId) {
        return commentService.getCommentsForArticle(articleId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred: " + ex.getMessage());
    }
} 