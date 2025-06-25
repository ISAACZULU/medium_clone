package user.service;

import java.util.List;

public interface RecommendationService {
    void generateRecommendationsForUser(String userEmail);
    void generateRecommendationsForAllUsers();
    List<Long> getRecommendedArticleIds(String userEmail, int limit);
} 