package user.controller;

import com.medium_clone.user.dto.ArticleResponse;
import com.medium_clone.user.dto.ArticleSearchRequest;
import com.medium_clone.user.dto.PersonalizedFeedRequest;
import user.service.ArticleDiscoveryService;
import user.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/discovery")
@Validated
public class ArticleDiscoveryController {

    private final ArticleDiscoveryService discoveryService;
    private final JwtUtil jwtUtil;

    @Autowired
    public ArticleDiscoveryController(ArticleDiscoveryService discoveryService, JwtUtil jwtUtil) {
        this.discoveryService = discoveryService;
        this.jwtUtil = jwtUtil;
    }

    // Personalized feed for logged-in users
    @PostMapping("/feed")
    public Page<ArticleResponse> getPersonalizedFeed(@Valid @RequestBody PersonalizedFeedRequest request,
                                                    @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return discoveryService.getPersonalizedFeed(email, request);
    }

    // Trending articles (public)
    @GetMapping("/trending")
    public Page<ArticleResponse> getTrendingArticles(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return discoveryService.getTrendingArticles(page, size);
    }

    // Advanced search (public)
    @PostMapping("/search")
    public Page<ArticleResponse> advancedSearch(@Valid @RequestBody ArticleSearchRequest request) {
        return discoveryService.advancedSearch(request);
    }

    // Search by tags (public)
    @PostMapping("/search/tags")
    public Page<ArticleResponse> searchByTags(@RequestBody Set<String> tags,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return discoveryService.searchByTags(tags, page, size);
    }

    // Recent articles (public)
    @GetMapping("/recent")
    public Page<ArticleResponse> getRecentArticles(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return discoveryService.getRecentArticles(page, size);
    }

    // Track engagement (requires authentication)
    @PostMapping("/articles/{articleId}/engage")
    public ResponseEntity<String> trackEngagement(@PathVariable Long articleId,
                                                 @RequestParam String type,
                                                 @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        discoveryService.trackEngagement(email, articleId, type);
        return ResponseEntity.ok("Engagement tracked successfully");
    }

    // Get engagement statistics for an article (public)
    @GetMapping("/articles/{articleId}/engagement")
    public Map<String, Long> getArticleEngagementStats(@PathVariable Long articleId) {
        return discoveryService.getArticleEngagementStats(articleId);
    }

    // Quick search by keywords (public)
    @GetMapping("/search/quick")
    public Page<ArticleResponse> quickSearch(@RequestParam String q,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        ArticleSearchRequest request = new ArticleSearchRequest();
        request.setKeywords(q);
        request.setPage(page);
        request.setSize(size);
        return discoveryService.advancedSearch(request);
    }

    // Get articles by author (public)
    @GetMapping("/author/{username}")
    public Page<ArticleResponse> getArticlesByAuthor(@PathVariable String username,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        ArticleSearchRequest request = new ArticleSearchRequest();
        request.setAuthorUsername(username);
        request.setPage(page);
        request.setSize(size);
        return discoveryService.advancedSearch(request);
    }

    // Get articles by date range (public)
    @GetMapping("/date-range")
    public Page<ArticleResponse> getArticlesByDateRange(@RequestParam String fromDate,
                                                       @RequestParam String toDate,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        ArticleSearchRequest request = new ArticleSearchRequest();
        // Note: You'd need to parse the date strings to LocalDateTime
        request.setPage(page);
        request.setSize(size);
        return discoveryService.advancedSearch(request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred: " + ex.getMessage());
    }
} 