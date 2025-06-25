package user.service;

import user.entity.Notification;
import user.entity.User;
import user.repository.NotificationRepository;
import user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmailNotificationServiceImpl implements EmailNotificationService {
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @Autowired
    public EmailNotificationServiceImpl(UserRepository userRepository, NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public void sendImmediateNotification(User user, Notification notification) {
        if (!user.isEmailNotificationsEnabled() || 
            user.getEmailDigestFrequency() != User.EmailDigestFrequency.IMMEDIATE) {
            return;
        }
        
        // Here you would integrate with your email service (SendGrid, AWS SES, etc.)
        String subject = "New notification from Medium Clone";
        String body = buildEmailBody(notification);
        
        // sendEmail(user.getEmail(), subject, body);
        System.out.println("Sending immediate email to " + user.getEmail() + ": " + subject);
    }

    @Override
    @Transactional
    public void sendDailyDigest(User user, List<Notification> notifications) {
        if (!user.isEmailNotificationsEnabled() || 
            user.getEmailDigestFrequency() != User.EmailDigestFrequency.DAILY) {
            return;
        }
        
        if (notifications.isEmpty()) {
            return;
        }
        
        String subject = "Your daily digest from Medium Clone";
        String body = buildDigestBody(notifications, "daily");
        
        // sendEmail(user.getEmail(), subject, body);
        System.out.println("Sending daily digest to " + user.getEmail() + ": " + notifications.size() + " notifications");
    }

    @Override
    @Transactional
    public void sendWeeklyDigest(User user, List<Notification> notifications) {
        if (!user.isEmailNotificationsEnabled() || 
            user.getEmailDigestFrequency() != User.EmailDigestFrequency.WEEKLY) {
            return;
        }
        
        if (notifications.isEmpty()) {
            return;
        }
        
        String subject = "Your weekly digest from Medium Clone";
        String body = buildDigestBody(notifications, "weekly");
        
        // sendEmail(user.getEmail(), subject, body);
        System.out.println("Sending weekly digest to " + user.getEmail() + ": " + notifications.size() + " notifications");
    }

    @Override
    @Transactional
    public void sendEmailDigestForAllUsers() {
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            try {
                LocalDateTime since = LocalDateTime.now().minusDays(1);
                if (user.getEmailDigestFrequency() == User.EmailDigestFrequency.WEEKLY) {
                    since = LocalDateTime.now().minusWeeks(1);
                }
                
                List<Notification> unreadNotifications = notificationRepository
                    .findByRecipientAndReadFalseAndCreatedAtAfterOrderByCreatedAtDesc(user, since);
                
                if (user.getEmailDigestFrequency() == User.EmailDigestFrequency.DAILY) {
                    sendDailyDigest(user, unreadNotifications);
                } else if (user.getEmailDigestFrequency() == User.EmailDigestFrequency.WEEKLY) {
                    sendWeeklyDigest(user, unreadNotifications);
                }
            } catch (Exception e) {
                System.err.println("Error sending digest to " + user.getEmail() + ": " + e.getMessage());
            }
        }
    }

    private String buildEmailBody(Notification notification) {
        StringBuilder body = new StringBuilder();
        body.append("Hello ").append(notification.getRecipient().getUsername()).append(",\n\n");
        body.append(notification.getMessage()).append("\n\n");
        body.append("Best regards,\nMedium Clone Team");
        return body.toString();
    }

    private String buildDigestBody(List<Notification> notifications, String frequency) {
        StringBuilder body = new StringBuilder();
        body.append("Hello ").append(notifications.get(0).getRecipient().getUsername()).append(",\n\n");
        body.append("Here's your ").append(frequency).append(" digest:\n\n");
        
        for (Notification notification : notifications) {
            body.append("• ").append(notification.getMessage()).append("\n");
        }
        
        body.append("\nBest regards,\nMedium Clone Team");
        return body.toString();
    }
} 