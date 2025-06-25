package user.controller;

import user.service.AdminService;
import user.entity.User;
import user.config.JwtUtil;
import user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Autowired
    public AdminController(AdminService adminService, JwtUtil jwtUtil, UserRepository userRepository) {
        this.adminService = adminService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    // User Management
    @GetMapping("/users")
    public List<User> getAllUsers(@RequestHeader("Authorization") String authHeader) {
        validateAdmin(authHeader);
        return adminService.getAllUsers();
    }

    @GetMapping("/users/{userId}")
    public User getUserById(@PathVariable Long userId, @RequestHeader("Authorization") String authHeader) {
        validateAdmin(authHeader);
        return adminService.getUserById(userId);
    }

    @PutMapping("/users/{userId}/role")
    public User updateUserRole(@PathVariable Long userId, @RequestBody Map<String, String> request, 
                              @RequestHeader("Authorization") String authHeader) {
        validateAdmin(authHeader);
        return adminService.updateUserRole(userId, request.get("role"));
    }

    @PostMapping("/users/{userId}/deactivate")
    public ResponseEntity<String> deactivateUser(@PathVariable Long userId, @RequestHeader("Authorization") String authHeader) {
        validateAdmin(authHeader);
        adminService.deactivateUser(userId);
        return ResponseEntity.ok("User deactivated successfully");
    }

    @PostMapping("/users/{userId}/activate")
    public ResponseEntity<String> activateUser(@PathVariable Long userId, @RequestHeader("Authorization") String authHeader) {
        validateAdmin(authHeader);
        adminService.activateUser(userId);
        return ResponseEntity.ok("User activated successfully");
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId, @RequestHeader("Authorization") String authHeader) {
        validateAdmin(authHeader);
        adminService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PostMapping("/users/{userId}/ban")
    public ResponseEntity<String> banUser(@PathVariable Long userId, @RequestBody Map<String, String> request, 
                                         @RequestHeader("Authorization") String authHeader) {
        validateAdmin(authHeader);
        adminService.banUser(userId, request.get("reason"));
        return ResponseEntity.ok("User banned successfully");
    }

    // Content Moderation
    @GetMapping("/moderation/comments/flagged")
    public List<Object> getFlaggedComments(@RequestHeader("Authorization") String authHeader) {
        validateAdmin(authHeader);
        return adminService.getFlaggedComments();
    }

    @PostMapping("/moderation/comments/{commentId}/approve")
    public ResponseEntity<String> approveComment(@PathVariable Long commentId, @RequestHeader("Authorization") String authHeader) {
        validateAdmin(authHeader);
        adminService.approveComment(commentId);
        return ResponseEntity.ok("Comment approved successfully");
    }

    @PostMapping("/moderation/comments/{commentId}/reject")
    public ResponseEntity<String> rejectComment(@PathVariable Long commentId, @RequestHeader("Authorization") String authHeader) {
        validateAdmin(authHeader);
        adminService.rejectComment(commentId);
        return ResponseEntity.ok("Comment rejected successfully");
    }

    @GetMapping("/moderation/articles/flagged")
    public List<Object> getFlaggedArticles(@RequestHeader("Authorization") String authHeader) {
        validateAdmin(authHeader);
        return adminService.getFlaggedArticles();
    }

    @PostMapping("/moderation/articles/{articleId}/approve")
    public ResponseEntity<String> approveArticle(@PathVariable Long articleId, @RequestHeader("Authorization") String authHeader) {
        validateAdmin(authHeader);
        adminService.approveArticle(articleId);
        return ResponseEntity.ok("Article approved successfully");
    }

    @PostMapping("/moderation/articles/{articleId}/reject")
    public ResponseEntity<String> rejectArticle(@PathVariable Long articleId, @RequestHeader("Authorization") String authHeader) {
        validateAdmin(authHeader);
        adminService.rejectArticle(articleId);
        return ResponseEntity.ok("Article rejected successfully");
    }

    // Analytics
    @GetMapping("/analytics/platform")
    public Map<String, Object> getPlatformAnalytics(@RequestHeader("Authorization") String authHeader) {
        validateAdmin(authHeader);
        return adminService.getPlatformAnalytics();
    }

    @GetMapping("/analytics/users")
    public Map<String, Object> getUserAnalytics(@RequestHeader("Authorization") String authHeader) {
        validateAdmin(authHeader);
        return adminService.getUserAnalytics();
    }

    @GetMapping("/analytics/content")
    public Map<String, Object> getContentAnalytics(@RequestHeader("Authorization") String authHeader) {
        validateAdmin(authHeader);
        return adminService.getContentAnalytics();
    }

    @GetMapping("/analytics/engagement")
    public Map<String, Object> getEngagementAnalytics(@RequestHeader("Authorization") String authHeader) {
        validateAdmin(authHeader);
        return adminService.getEngagementAnalytics();
    }

    private void validateAdmin(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        if (!user.isAdmin()) {
            throw new IllegalArgumentException("Admin access required");
        }
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