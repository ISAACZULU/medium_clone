package user.controller;

import com.medium_clone.user.dto.CreateArticleRequest;
import com.medium_clone.user.dto.ArticleResponse;
import com.medium_clone.user.dto.ArticleVersionResponse;
import com.medium_clone.user.dto.UpdateArticleRequest;
import com.medium_clone.user.service.ArticleService;
import user.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/articles")
@Validated
public class ArticleController {

    private final ArticleService articleService;
    private final JwtUtil jwtUtil;

    @Autowired
    public ArticleController(ArticleService articleService, JwtUtil jwtUtil) {
        this.articleService = articleService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleResponse createArticle(@Valid @RequestBody CreateArticleRequest request,
                                       @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return articleService.createArticle(email, request);
    }

    @PutMapping("/{id}")
    public ArticleResponse updateArticle(@PathVariable Long id,
                                       @Valid @RequestBody CreateArticleRequest request,
                                       @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return articleService.updateArticle(email, id, request);
    }

    @GetMapping("/{slug}")
    public ArticleResponse getArticleBySlug(@PathVariable String slug) {
        return articleService.getArticleBySlug(slug);
    }

    @GetMapping("/id/{id}")
    public ArticleResponse getArticleById(@PathVariable Long id) {
        return articleService.getArticleById(id);
    }

    @GetMapping("/author/{username}")
    public Page<ArticleResponse> getArticlesByAuthor(@PathVariable String username,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return articleService.getArticlesByAuthor(username, pageable);
    }

    @GetMapping
    public Page<ArticleResponse> getPublishedArticles(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return articleService.getPublishedArticles(pageable);
    }

    @GetMapping("/tag/{tag}")
    public Page<ArticleResponse> getArticlesByTag(@PathVariable String tag,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return articleService.getArticlesByTag(tag, pageable);
    }

    @GetMapping("/search")
    public Page<ArticleResponse> searchArticles(@RequestParam String q,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return articleService.searchArticles(q, pageable);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteArticle(@PathVariable Long id,
                                               @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        articleService.deleteArticle(email, id);
        return ResponseEntity.ok("Article deleted successfully");
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<String> publishArticle(@PathVariable Long id,
                                                @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        articleService.publishArticle(email, id);
        return ResponseEntity.ok("Article published successfully");
    }

    @PostMapping("/{id}/unpublish")
    public ResponseEntity<String> unpublishArticle(@PathVariable Long id,
                                                  @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        articleService.unpublishArticle(email, id);
        return ResponseEntity.ok("Article unpublished successfully");
    }

    // Versioning endpoints
    @GetMapping("/{id}/versions")
    public List<ArticleVersionResponse> getArticleVersions(@PathVariable Long id) {
        return articleService.getArticleVersions(id);
    }

    @GetMapping("/{id}/versions/{versionNumber}")
    public ArticleVersionResponse getArticleVersion(@PathVariable Long id, @PathVariable Integer versionNumber) {
        return articleService.getArticleVersion(id, versionNumber);
    }

    @PostMapping("/{id}/versions/{versionNumber}/restore")
    public ArticleResponse restoreArticleVersion(@PathVariable Long id, @PathVariable Integer versionNumber,
                                                @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return articleService.restoreArticleVersion(email, id, versionNumber);
    }

    @DeleteMapping("/{id}/versions/{versionNumber}")
    public ResponseEntity<String> deleteArticleVersion(@PathVariable Long id, @PathVariable Integer versionNumber,
                                                      @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        articleService.deleteArticleVersion(email, id, versionNumber);
        return ResponseEntity.ok("Version deleted successfully");
    }

    @PutMapping("/{id}/update-with-version")
    public ArticleResponse updateArticleWithVersion(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateArticleRequest request,
                                                   @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return articleService.updateArticleWithVersion(email, id, request);
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