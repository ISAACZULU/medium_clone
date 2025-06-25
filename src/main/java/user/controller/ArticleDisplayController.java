package user.controller;

import com.medium_clone.user.dto.ArticleResponse;
import user.service.ArticleService;
import user.service.ArticleDiscoveryService;
import user.util.ArticleUtils;
import user.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/display")
public class ArticleDisplayController {

    private final ArticleService articleService;
    private final ArticleDiscoveryService discoveryService;
    private final JwtUtil jwtUtil;

    @Autowired
    public ArticleDisplayController(ArticleService articleService, 
                                  ArticleDiscoveryService discoveryService,
                                  JwtUtil jwtUtil) {
        this.articleService = articleService;
        this.discoveryService = discoveryService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Get article by slug with enhanced display information
     * This endpoint automatically tracks views and provides engagement metrics
     */
    @GetMapping("/articles/{slug}")
    public ResponseEntity<Map<String, Object>> getArticleForDisplay(@PathVariable String slug,
                                                                   @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Get the article
            ArticleResponse article = articleService.getArticleBySlug(slug);
            
            // Track view if user is authenticated
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.replace("Bearer ", "");
                    String email = jwtUtil.extractEmail(token);
                    discoveryService.trackEngagement(email, article.getId(), "VIEW");
                } catch (Exception e) {
                    // Continue without tracking if token is invalid
                }
            }

            // Get engagement statistics
            Map<String, Long> engagementStats = discoveryService.getArticleEngagementStats(article.getId());
            
            // Create response with enhanced display information
            Map<String, Object> response = new HashMap<>();
            response.put("article", article);
            response.put("engagement", engagementStats);
            response.put("displayInfo", createDisplayInfo(article, engagementStats));
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Article not found"));
        }
    }

    /**
     * Get article preview (without tracking view)
     */
    @GetMapping("/articles/{slug}/preview")
    public ArticleResponse getArticlePreview(@PathVariable String slug) {
        return articleService.getArticleBySlug(slug);
    }

    /**
     * Get article statistics and engagement metrics
     */
    @GetMapping("/articles/{slug}/stats")
    public ResponseEntity<Map<String, Object>> getArticleStats(@PathVariable String slug) {
        try {
            ArticleResponse article = articleService.getArticleBySlug(slug);
            Map<String, Long> engagementStats = discoveryService.getArticleEngagementStats(article.getId());
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("viewCount", article.getViewCount());
            stats.put("formattedViewCount", ArticleUtils.formatViewCount(article.getViewCount()));
            stats.put("readTime", article.getReadTime());
            stats.put("engagement", engagementStats);
            stats.put("engagementRate", calculateEngagementRate(article.getViewCount(), engagementStats));
            
            return ResponseEntity.ok(stats);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Article not found"));
        }
    }

    /**
     * Track article engagement (like, bookmark, share)
     */
    @PostMapping("/articles/{slug}/engage")
    public ResponseEntity<String> trackArticleEngagement(@PathVariable String slug,
                                                        @RequestParam String type,
                                                        @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);
            
            ArticleResponse article = articleService.getArticleBySlug(slug);
            discoveryService.trackEngagement(email, article.getId(), type);
            
            return ResponseEntity.ok("Engagement tracked successfully");
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid engagement type or article not found");
        }
    }

    /**
     * Get reading progress for an article
     */
    @GetMapping("/articles/{slug}/progress")
    public ResponseEntity<Map<String, Object>> getReadingProgress(@PathVariable String slug,
                                                                 @RequestParam(defaultValue = "0") int scrollPercentage,
                                                                 @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtUtil.extractEmail(token);
            
            ArticleResponse article = articleService.getArticleBySlug(slug);
            
            // Track reading progress
            if (scrollPercentage > 50) {
                discoveryService.trackEngagement(email, article.getId(), "READ");
            }
            
            Map<String, Object> progress = new HashMap<>();
            progress.put("scrollPercentage", scrollPercentage);
            progress.put("estimatedTimeRemaining", calculateTimeRemaining(article.getReadTime(), scrollPercentage));
            progress.put("isCompleted", scrollPercentage >= 90);
            
            return ResponseEntity.ok(progress);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Article not found"));
        }
    }

    /**
     * Get related articles based on tags and author
     */
    @GetMapping("/articles/{slug}/related")
    public ResponseEntity<Map<String, Object>> getRelatedArticles(@PathVariable String slug,
                                                                 @RequestParam(defaultValue = "5") int limit) {
        try {
            ArticleResponse article = articleService.getArticleBySlug(slug);
            
            // This would typically use a more sophisticated algorithm
            // For now, we'll return a simple response
            Map<String, Object> related = new HashMap<>();
            related.put("articleId", article.getId());
            related.put("tags", article.getTags());
            related.put("author", article.getAuthorUsername());
            related.put("message", "Related articles would be fetched based on tags and author");
            
            return ResponseEntity.ok(related);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Article not found"));
        }
    }

    /**
     * Validate slug format
     */
    @GetMapping("/validate-slug/{slug}")
    public ResponseEntity<Map<String, Object>> validateSlug(@PathVariable String slug) {
        Map<String, Object> response = new HashMap<>();
        response.put("slug", slug);
        response.put("isValid", ArticleUtils.isValidSlug(slug));
        response.put("suggestions", generateSlugSuggestions(slug));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Calculate read time for content
     */
    @PostMapping("/calculate-read-time")
    public ResponseEntity<Map<String, Object>> calculateReadTime(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        String contentType = request.getOrDefault("contentType", "blog");
        
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Content is required"));
        }
        
        int readTime = ArticleUtils.calculateReadTimeForContent(content, contentType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("readTime", readTime);
        response.put("contentType", contentType);
        response.put("wordCount", content.split("\\s+").length);
        
        return ResponseEntity.ok(response);
    }

    // Helper methods

    private Map<String, Object> createDisplayInfo(ArticleResponse article, Map<String, Long> engagementStats) {
        Map<String, Object> displayInfo = new HashMap<>();
        displayInfo.put("formattedViewCount", ArticleUtils.formatViewCount(article.getViewCount()));
        displayInfo.put("readTimeText", formatReadTime(article.getReadTime()));
        displayInfo.put("engagementRate", calculateEngagementRate(article.getViewCount(), engagementStats));
        displayInfo.put("isPopular", article.getViewCount() > 1000);
        displayInfo.put("isTrending", article.getViewCount() > 5000);
        
        return displayInfo;
    }

    private String formatReadTime(int minutes) {
        if (minutes < 1) {
            return "Less than 1 min read";
        } else if (minutes == 1) {
            return "1 min read";
        } else {
            return minutes + " min read";
        }
    }

    private double calculateEngagementRate(int viewCount, Map<String, Long> engagementStats) {
        long likes = engagementStats.getOrDefault("like", 0L);
        long comments = engagementStats.getOrDefault("comment", 0L);
        long shares = engagementStats.getOrDefault("share", 0L);
        
        return ArticleUtils.calculateEngagementRate(viewCount, (int) likes, (int) comments, (int) shares);
    }

    private int calculateTimeRemaining(int totalMinutes, int scrollPercentage) {
        int remainingPercentage = 100 - scrollPercentage;
        return (int) Math.ceil((totalMinutes * remainingPercentage) / 100.0);
    }

    private String[] generateSlugSuggestions(String invalidSlug) {
        // Simple suggestions for invalid slugs
        return new String[]{
            ArticleUtils.generateSlug(invalidSlug),
            invalidSlug.toLowerCase().replaceAll("[^a-z0-9]", "-"),
            invalidSlug.toLowerCase().replaceAll("\\s+", "-")
        };
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred: " + ex.getMessage());
    }
} 