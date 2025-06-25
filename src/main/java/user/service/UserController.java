package user.service;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/me/notification-preferences")
    public NotificationPreferencesResponse getNotificationPreferences(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        User user = userService.getUserByEmail(email);
        NotificationPreferencesResponse response = new NotificationPreferencesResponse();
        response.setReceiveFollowNotifications(user.isReceiveFollowNotifications());
        response.setReceiveClapNotifications(user.isReceiveClapNotifications());
        response.setReceiveCommentNotifications(user.isReceiveCommentNotifications());
        response.setReceiveMentionNotifications(user.isReceiveMentionNotifications());
        response.setReceiveRecommendationNotifications(user.isReceiveRecommendationNotifications());
        response.setEmailNotificationsEnabled(user.isEmailNotificationsEnabled());
        response.setPushNotificationsEnabled(user.isPushNotificationsEnabled());
        response.setEmailDigestFrequency(user.getEmailDigestFrequency().name());
        return response;
    }
} 