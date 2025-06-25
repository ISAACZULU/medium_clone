package user.service;

import user.entity.Article;
import user.entity.User;
import user.repository.ArticleRepository;
import user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public RecommendationServiceImpl(ArticleRepository articleRepository, UserRepository userRepository, NotificationService notificationService) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void generateRecommendationsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        List<Long> recommendedArticleIds = getRecommendedArticleIds(userEmail, 3);
        
        for (Long articleId : recommendedArticleIds) {
            Article article = articleRepository.findById(articleId)
                    .orElseThrow(() -> new IllegalArgumentException("Article not found"));
            notificationService.notifyRecommendation(user, article, "Based on your interests");
        }
    }

    @Override
    @Transactional
    public void generateRecommendationsForAllUsers() {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            try {
                generateRecommendationsForUser(user.getEmail());
            } catch (Exception e) {
                // Log error but continue with other users
                System.err.println("Error generating recommendations for user: " + user.getEmail() + " - " + e.getMessage());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getRecommendedArticleIds(String userEmail, int limit) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Get articles from followed authors
        List<String> followedUsernames = user.getFollowing().stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
        
        List<Article> recommendedArticles = articleRepository.findByAuthorUsernameInAndPublishedTrueOrderByPublishedAtDesc(followedUsernames);
        
        // If not enough articles from followed authors, add trending articles
        if (recommendedArticles.size() < limit) {
            List<Article> trendingArticles = articleRepository.findByPublishedTrueOrderByViewCountDesc();
            recommendedArticles.addAll(trendingArticles.stream()
                    .filter(article -> !recommendedArticles.contains(article))
                    .limit(limit - recommendedArticles.size())
                    .collect(Collectors.toList()));
        }
        
        return recommendedArticles.stream()
                .limit(limit)
                .map(Article::getId)
                .collect(Collectors.toList());
    }
} 