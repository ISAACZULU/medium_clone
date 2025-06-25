package user.service;

import user.entity.Notification;
import user.entity.User;
import java.util.List;

public interface EmailNotificationService {
    void sendImmediateNotification(User user, Notification notification);
    void sendDailyDigest(User user, List<Notification> notifications);
    void sendWeeklyDigest(User user, List<Notification> notifications);
    void sendEmailDigestForAllUsers();
} 