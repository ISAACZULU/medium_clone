package user.controller;

import user.service.RecommendationService;
import user.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService recommendationService;
    private final JwtUtil jwtUtil;

    @Autowired
    public RecommendationController(RecommendationService recommendationService, JwtUtil jwtUtil) {
        this.recommendationService = recommendationService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateRecommendations(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        recommendationService.generateRecommendationsForUser(email);
        return ResponseEntity.ok("Recommendations generated successfully");
    }

    @GetMapping("/articles")
    public List<Long> getRecommendedArticles(@RequestHeader("Authorization") String authHeader, 
                                             @RequestParam(defaultValue = "5") int limit) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return recommendationService.getRecommendedArticleIds(email, limit);
    }

    @PostMapping("/generate-all")
    public ResponseEntity<String> generateRecommendationsForAllUsers() {
        recommendationService.generateRecommendationsForAllUsers();
        return ResponseEntity.ok("Recommendations generated for all users");
    }
} 