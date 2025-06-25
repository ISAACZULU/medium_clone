package user.controller;

import user.entity.Notification;
import user.entity.User;
import user.service.NotificationService;
import user.repository.UserRepository;
import user.config.JwtUtil;
import user.dto.NotificationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public NotificationController(NotificationService notificationService, UserRepository userRepository, JwtUtil jwtUtil) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public List<NotificationResponse> getNotifications(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return notificationService.getNotifications(user).stream().map(this::toDto).toList();
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        notificationService.markAsRead(id, user);
        return ResponseEntity.ok("Notification marked as read");
    }

    @GetMapping("/unread-count")
    public long countUnread(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return notificationService.countUnread(user);
    }

    private NotificationResponse toDto(Notification n) {
        NotificationResponse dto = new NotificationResponse();
        dto.setId(n.getId());
        dto.setType(n.getType().name());
        dto.setMessage(n.getMessage());
        dto.setRelatedUser(n.getRelatedUser() != null ? n.getRelatedUser().getUsername() : null);
        dto.setRelatedArticleId(n.getRelatedArticle() != null ? n.getRelatedArticle().getId() : null);
        dto.setRelatedArticleTitle(n.getRelatedArticle() != null ? n.getRelatedArticle().getTitle() : null);
        dto.setRelatedCommentId(n.getRelatedComment() != null ? n.getRelatedComment().getId() : null);
        dto.setRead(n.isRead());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
} 