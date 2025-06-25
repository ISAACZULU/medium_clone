package user.config;

import user.service.RecommendationService;
import user.service.EmailNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class ScheduledTasksConfig {
    private final RecommendationService recommendationService;
    private final EmailNotificationService emailNotificationService;

    @Autowired
    public ScheduledTasksConfig(RecommendationService recommendationService, EmailNotificationService emailNotificationService) {
        this.recommendationService = recommendationService;
        this.emailNotificationService = emailNotificationService;
    }

    // Generate recommendations for all users every day at 9 AM
    @Scheduled(cron = "0 0 9 * * ?")
    public void generateDailyRecommendations() {
        try {
            recommendationService.generateRecommendationsForAllUsers();
        } catch (Exception e) {
            // Log error but don't fail the scheduled task
            System.err.println("Error in scheduled recommendation generation: " + e.getMessage());
        }
    }

    // Send email digests daily at 8 AM
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendDailyEmailDigests() {
        try {
            emailNotificationService.sendEmailDigestForAllUsers();
        } catch (Exception e) {
            System.err.println("Error in scheduled email digest: " + e.getMessage());
        }
    }

    // Send weekly email digests every Monday at 9 AM
    @Scheduled(cron = "0 0 9 ? * MON")
    public void sendWeeklyEmailDigests() {
        try {
            emailNotificationService.sendEmailDigestForAllUsers();
        } catch (Exception e) {
            System.err.println("Error in scheduled weekly email digest: " + e.getMessage());
        }
    }
} 