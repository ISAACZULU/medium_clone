package user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_username", columnList = "username")
        })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 255)
    @org.hibernate.annotations.Type(type = "org.hibernate.type.StringType")
    private String email;

    @Column(unique = true, nullable = false, length = 50)
    @org.hibernate.annotations.Type(type = "org.hibernate.type.StringType")
    private String username;

    @Column(nullable = false)
    @org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
    private String password;

    @Column(nullable = false)
    private boolean active = true;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
            name = "user_following",
            joinColumns = @JoinColumn(name = "follower_id"),
            inverseJoinColumns = @JoinColumn(name = "following_id")
    )
    @Builder.Default
    private Set<User> following = new HashSet<>();

    @ManyToMany(mappedBy = "following")
    @Builder.Default
    private Set<User> followers = new HashSet<>();

    @Column
    private String bio;

    @Column
    private String image;

    private boolean receiveFollowNotifications = true;
    private boolean receiveClapNotifications = true;
    private boolean receiveCommentNotifications = true;
    private boolean receiveMentionNotifications = true;
    private boolean receiveRecommendationNotifications = true;
    private boolean emailNotificationsEnabled = true;
    private boolean pushNotificationsEnabled = true;
    private EmailDigestFrequency emailDigestFrequency = EmailDigestFrequency.DAILY;
    private UserRole role = UserRole.USER;

    public boolean isReceiveFollowNotifications() { return receiveFollowNotifications; }
    public void setReceiveFollowNotifications(boolean receiveFollowNotifications) { this.receiveFollowNotifications = receiveFollowNotifications; }
    public boolean isReceiveClapNotifications() { return receiveClapNotifications; }
    public void setReceiveClapNotifications(boolean receiveClapNotifications) { this.receiveClapNotifications = receiveClapNotifications; }
    public boolean isReceiveCommentNotifications() { return receiveCommentNotifications; }
    public void setReceiveCommentNotifications(boolean receiveCommentNotifications) { this.receiveCommentNotifications = receiveCommentNotifications; }
    public boolean isReceiveMentionNotifications() { return receiveMentionNotifications; }
    public void setReceiveMentionNotifications(boolean receiveMentionNotifications) { this.receiveMentionNotifications = receiveMentionNotifications; }
    public boolean isReceiveRecommendationNotifications() { return receiveRecommendationNotifications; }
    public void setReceiveRecommendationNotifications(boolean receiveRecommendationNotifications) { this.receiveRecommendationNotifications = receiveRecommendationNotifications; }
    public boolean isEmailNotificationsEnabled() { return emailNotificationsEnabled; }
    public void setEmailNotificationsEnabled(boolean emailNotificationsEnabled) { this.emailNotificationsEnabled = emailNotificationsEnabled; }
    public boolean isPushNotificationsEnabled() { return pushNotificationsEnabled; }
    public void setPushNotificationsEnabled(boolean pushNotificationsEnabled) { this.pushNotificationsEnabled = pushNotificationsEnabled; }
    public EmailDigestFrequency getEmailDigestFrequency() { return emailDigestFrequency; }
    public void setEmailDigestFrequency(EmailDigestFrequency emailDigestFrequency) { this.emailDigestFrequency = emailDigestFrequency; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public boolean isAdmin() { return role == UserRole.ADMIN; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public enum EmailDigestFrequency {
        NEVER,      // No email notifications
        IMMEDIATE,  // Send immediately
        DAILY,      // Daily digest
        WEEKLY      // Weekly digest
    }

    public enum UserRole {
        USER, ADMIN
    }
}
