package user.service;

import user.entity.Notification;
import user.entity.User;
import user.repository.NotificationRepository;
import user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import user.dto.NotificationResponse;
import user.entity.Article;
import user.entity.Comment;
import user.service.EmailNotificationService;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailNotificationService emailNotificationService;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository, SimpMessagingTemplate messagingTemplate, EmailNotificationService emailNotificationService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.emailNotificationService = emailNotificationService;
    }

    @Override
    @Transactional
    public void notifyFollow(User recipient, User follower) {
        if (!recipient.isReceiveFollowNotifications()) return;
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(Notification.NotificationType.FOLLOW)
                .message(follower.getUsername() + " started following you.")
                .relatedUser(follower)
                .read(false)
                .build();
        Notification saved = notificationRepository.save(notification);
        
        // Send real-time notification
        NotificationResponse dto = new NotificationResponse();
        dto.setId(saved.getId());
        dto.setType(saved.getType().name());
        dto.setMessage(saved.getMessage());
        dto.setRelatedUser(follower.getUsername());
        dto.setRead(false);
        dto.setCreatedAt(saved.getCreatedAt());
        messagingTemplate.convertAndSend("/topic/notifications/" + recipient.getId(), dto);
        
        // Send email notification if enabled
        emailNotificationService.sendImmediateNotification(recipient, saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotifications(User recipient) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, User recipient) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.getRecipient().getId().equals(recipient.getId())) {
            throw new IllegalArgumentException("Not your notification");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(User recipient) {
        return notificationRepository.countByRecipientAndReadFalse(recipient);
    }

    @Override
    @Transactional
    public void notifyClap(User recipient, User clapper, Article article) {
        if (!recipient.isReceiveClapNotifications()) return;
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(Notification.NotificationType.CLAP)
                .message(clapper.getUsername() + " clapped your article: " + article.getTitle())
                .relatedUser(clapper)
                .relatedArticle(article)
                .read(false)
                .build();
        Notification saved = notificationRepository.save(notification);
        NotificationResponse dto = new NotificationResponse();
        dto.setId(saved.getId());
        dto.setType(saved.getType().name());
        dto.setMessage(saved.getMessage());
        dto.setRelatedUser(clapper.getUsername());
        dto.setRelatedArticleId(article.getId());
        dto.setRelatedArticleTitle(article.getTitle());
        dto.setRead(false);
        dto.setCreatedAt(saved.getCreatedAt());
        messagingTemplate.convertAndSend("/topic/notifications/" + recipient.getId(), dto);
    }

    @Override
    @Transactional
    public void notifyComment(User recipient, User commenter, Article article, Comment comment) {
        if (!recipient.isReceiveCommentNotifications()) return;
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(Notification.NotificationType.COMMENT)
                .message(commenter.getUsername() + " commented on your article: " + article.getTitle())
                .relatedUser(commenter)
                .relatedArticle(article)
                .relatedComment(comment)
                .read(false)
                .build();
        Notification saved = notificationRepository.save(notification);
        NotificationResponse dto = new NotificationResponse();
        dto.setId(saved.getId());
        dto.setType(saved.getType().name());
        dto.setMessage(saved.getMessage());
        dto.setRelatedUser(commenter.getUsername());
        dto.setRelatedArticleId(article.getId());
        dto.setRelatedArticleTitle(article.getTitle());
        dto.setRelatedCommentId(comment.getId());
        dto.setRead(false);
        dto.setCreatedAt(saved.getCreatedAt());
        messagingTemplate.convertAndSend("/topic/notifications/" + recipient.getId(), dto);
    }

    @Override
    @Transactional
    public void notifyMention(User mentioned, User commenter, Article article, Comment comment) {
        if (!mentioned.isReceiveMentionNotifications()) return;
        Notification notification = Notification.builder()
                .recipient(mentioned)
                .type(Notification.NotificationType.MENTION)
                .message(commenter.getUsername() + " mentioned you in a comment on: " + article.getTitle())
                .relatedUser(commenter)
                .relatedArticle(article)
                .relatedComment(comment)
                .read(false)
                .build();
        Notification saved = notificationRepository.save(notification);
        NotificationResponse dto = new NotificationResponse();
        dto.setId(saved.getId());
        dto.setType(saved.getType().name());
        dto.setMessage(saved.getMessage());
        dto.setRelatedUser(commenter.getUsername());
        dto.setRelatedArticleId(article.getId());
        dto.setRelatedArticleTitle(article.getTitle());
        dto.setRelatedCommentId(comment.getId());
        dto.setRead(false);
        dto.setCreatedAt(saved.getCreatedAt());
        messagingTemplate.convertAndSend("/topic/notifications/" + mentioned.getId(), dto);
    }

    @Override
    @Transactional
    public void notifyRecommendation(User recipient, Article article, String reason) {
        if (!recipient.isReceiveRecommendationNotifications()) return;
        Notification notification = Notification.builder()
            .recipient(recipient)
            .type(Notification.NotificationType.RECOMMENDATION)
            .message("Recommended: " + article.getTitle() + (reason != null ? " (" + reason + ")" : ""))
            .relatedArticle(article)
            .read(false)
            .build();
        Notification saved = notificationRepository.save(notification);
        NotificationResponse dto = new NotificationResponse();
        dto.setId(saved.getId());
        dto.setType(saved.getType().name());
        dto.setMessage(saved.getMessage());
        dto.setRelatedArticleId(article.getId());
        dto.setRelatedArticleTitle(article.getTitle());
        dto.setRead(false);
        dto.setCreatedAt(saved.getCreatedAt());
        messagingTemplate.convertAndSend("/topic/notifications/" + recipient.getId(), dto);
    }
} 