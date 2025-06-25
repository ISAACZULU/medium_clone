package user.controller;

import com.medium_clone.user.dto.ArticleResponse;
import user.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/share")
public class ShareController {
    private final ArticleService articleService;
    private final String baseUrl;

    @Autowired
    public ShareController(ArticleService articleService, @Value("${app.share.base-url}") String baseUrl) {
        this.articleService = articleService;
        this.baseUrl = baseUrl;
    }

    @GetMapping("/{slug}")
    public ResponseEntity<Map<String, Object>> getShareMetadata(@PathVariable String slug) {
        try {
            ArticleResponse article = articleService.getArticleBySlug(slug);
            String url = baseUrl + article.getSlug();

            Map<String, String> openGraph = new HashMap<>();
            openGraph.put("og:title", article.getTitle());
            openGraph.put("og:description", article.getSummary());
            openGraph.put("og:image", article.getCoverImageUrl());
            openGraph.put("og:url", url);
            openGraph.put("og:type", "article");
            openGraph.put("og:site_name", "Medium Clone");

            Map<String, String> twitterCard = new HashMap<>();
            twitterCard.put("twitter:card", "summary_large_image");
            twitterCard.put("twitter:title", article.getTitle());
            twitterCard.put("twitter:description", article.getSummary());
            twitterCard.put("twitter:image", article.getCoverImageUrl());
            twitterCard.put("twitter:url", url);

            Map<String, Object> response = new HashMap<>();
            response.put("url", url);
            response.put("title", article.getTitle());
            response.put("description", article.getSummary());
            response.put("image", article.getCoverImageUrl());
            response.put("author", article.getAuthorUsername());
            response.put("openGraph", openGraph);
            response.put("twitterCard", twitterCard);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Article not found"));
        }
    }
} 