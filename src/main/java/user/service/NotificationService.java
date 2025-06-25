package user.service;

import user.entity.Notification;
import user.entity.User;
import user.entity.Article;
import user.entity.Comment;
import java.util.List;

public interface NotificationService {
    void notifyFollow(User recipient, User follower);
    List<Notification> getNotifications(User recipient);
    void markAsRead(Long notificationId, User recipient);
    long countUnread(User recipient);
    void notifyClap(User recipient, User clapper, Article article);
    void notifyComment(User recipient, User commenter, Article article, Comment comment);
    void notifyMention(User mentioned, User commenter, Article article, Comment comment);
} 