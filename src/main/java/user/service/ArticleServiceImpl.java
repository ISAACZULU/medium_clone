package user.service;

import com.medium_clone.user.dto.CreateArticleRequest;
import com.medium_clone.user.dto.UpdateArticleRequest;
import com.medium_clone.user.dto.ArticleResponse;
import com.medium_clone.user.dto.ArticleVersionResponse;
import com.medium_clone.user.entity.Article;
import com.medium_clone.user.entity.ArticleVersion;
import com.medium_clone.user.entity.User;
import user.repository.ArticleRepository;
import user.repository.ArticleVersionRepository;
import user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleVersionRepository articleVersionRepository;
    private final UserRepository userRepository;

    @Autowired
    public ArticleServiceImpl(ArticleRepository articleRepository, 
                            ArticleVersionRepository articleVersionRepository,
                            UserRepository userRepository) {
        this.articleRepository = articleRepository;
        this.articleVersionRepository = articleVersionRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ArticleResponse createArticle(String authorEmail, CreateArticleRequest request) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String slug = generateSlug(request.getTitle());
        
        // Check if slug already exists
        if (articleRepository.findBySlug(slug).isPresent()) {
            slug = generateUniqueSlug(slug);
        }

        Article article = Article.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .summary(request.getSummary())
                .tags(request.getTags() != null ? request.getTags() : new java.util.HashSet<>())
                .coverImageUrl(request.getCoverImageUrl())
                .slug(slug)
                .published(request.isPublished())
                .author(author)
                .readTime(calculateReadTime(request.getContent()))
                .viewCount(0)
                .build();

        if (request.isPublished()) {
            article.setPublishedAt(LocalDateTime.now());
        }

        Article savedArticle = articleRepository.save(article);
        
        // Create initial version
        createArticleVersion(savedArticle, authorEmail, "Initial version");
        
        return mapToArticleResponse(savedArticle);
    }

    @Override
    @Transactional
    public ArticleResponse updateArticle(String authorEmail, Long articleId, CreateArticleRequest request) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));

        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!article.getAuthor().getId().equals(author.getId())) {
            throw new IllegalArgumentException("You can only update your own articles");
        }

        // Create version before updating
        createArticleVersion(article, authorEmail, "Article updated");

        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setTags(request.getTags() != null ? request.getTags() : new java.util.HashSet<>());
        article.setCoverImageUrl(request.getCoverImageUrl());
        article.setReadTime(calculateReadTime(request.getContent()));

        // Update slug if title changed
        String newSlug = generateSlug(request.getTitle());
        if (!newSlug.equals(article.getSlug())) {
            if (articleRepository.findBySlug(newSlug).isPresent()) {
                newSlug = generateUniqueSlug(newSlug);
            }
            article.setSlug(newSlug);
        }

        // Handle publish status
        if (request.isPublished() && !article.isPublished()) {
            article.setPublished(true);
            article.setPublishedAt(LocalDateTime.now());
        } else if (!request.isPublished() && article.isPublished()) {
            article.setPublished(false);
            article.setPublishedAt(null);
        }

        Article updatedArticle = articleRepository.save(article);
        return mapToArticleResponse(updatedArticle);
    }

    @Override
    @Transactional
    public ArticleResponse updateArticleWithVersion(String authorEmail, Long articleId, UpdateArticleRequest request) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));

        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!article.getAuthor().getId().equals(author.getId())) {
            throw new IllegalArgumentException("You can only update your own articles");
        }

        // Create version before updating
        String changeDescription = request.getChangeDescription() != null ? 
            request.getChangeDescription() : "Article updated";
        createArticleVersion(article, authorEmail, changeDescription);

        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setSummary(request.getSummary());
        article.setTags(request.getTags() != null ? request.getTags() : new java.util.HashSet<>());
        article.setCoverImageUrl(request.getCoverImageUrl());
        article.setReadTime(calculateReadTime(request.getContent()));

        // Update slug if title changed
        String newSlug = generateSlug(request.getTitle());
        if (!newSlug.equals(article.getSlug())) {
            if (articleRepository.findBySlug(newSlug).isPresent()) {
                newSlug = generateUniqueSlug(newSlug);
            }
            article.setSlug(newSlug);
        }

        // Handle publish status
        if (request.isPublished() && !article.isPublished()) {
            article.setPublished(true);
            article.setPublishedAt(LocalDateTime.now());
        } else if (!request.isPublished() && article.isPublished()) {
            article.setPublished(false);
            article.setPublishedAt(null);
        }

        Article updatedArticle = articleRepository.save(article);
        return mapToArticleResponse(updatedArticle);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleResponse getArticleBySlug(String slug) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));
        
        if (!article.isPublished()) {
            throw new IllegalArgumentException("Article not found");
        }

        // Increment view count
        article.setViewCount(article.getViewCount() + 1);
        articleRepository.save(article);

        return mapToArticleResponse(article);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleResponse getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));
        return mapToArticleResponse(article);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleResponse> getArticlesByAuthor(String username, Pageable pageable) {
        Page<Article> articles = articleRepository.findByAuthorUsername(username, pageable);
        return articles.map(this::mapToArticleResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleResponse> getPublishedArticles(Pageable pageable) {
        Page<Article> articles = articleRepository.findByPublishedTrue(pageable);
        return articles.map(this::mapToArticleResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleResponse> getArticlesByTag(String tag, Pageable pageable) {
        Page<Article> articles = articleRepository.findByTag(tag, pageable);
        return articles.map(this::mapToArticleResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleResponse> searchArticles(String search, Pageable pageable) {
        Page<Article> articles = articleRepository.searchArticles(search, pageable);
        return articles.map(this::mapToArticleResponse);
    }

    @Override
    @Transactional
    public void deleteArticle(String authorEmail, Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));

        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!article.getAuthor().getId().equals(author.getId())) {
            throw new IllegalArgumentException("You can only delete your own articles");
        }

        // Delete all versions first
        articleVersionRepository.deleteByArticleId(articleId);
        articleRepository.delete(article);
    }

    @Override
    @Transactional
    public void publishArticle(String authorEmail, Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));

        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!article.getAuthor().getId().equals(author.getId())) {
            throw new IllegalArgumentException("You can only publish your own articles");
        }

        article.setPublished(true);
        article.setPublishedAt(LocalDateTime.now());
        articleRepository.save(article);
    }

    @Override
    @Transactional
    public void unpublishArticle(String authorEmail, Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));

        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!article.getAuthor().getId().equals(author.getId())) {
            throw new IllegalArgumentException("You can only unpublish your own articles");
        }

        article.setPublished(false);
        article.setPublishedAt(null);
        articleRepository.save(article);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleVersionResponse> getArticleVersions(Long articleId) {
        List<ArticleVersion> versions = articleVersionRepository.findByArticleIdOrderByVersionNumberDesc(articleId);
        return versions.stream()
                .map(this::mapToArticleVersionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleVersionResponse getArticleVersion(Long articleId, Integer versionNumber) {
        ArticleVersion version = articleVersionRepository.findByArticleIdAndVersionNumber(articleId, versionNumber)
                .orElseThrow(() -> new IllegalArgumentException("Version not found"));
        return mapToArticleVersionResponse(version);
    }

    @Override
    @Transactional
    public ArticleResponse restoreArticleVersion(String authorEmail, Long articleId, Integer versionNumber) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));

        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!article.getAuthor().getId().equals(author.getId())) {
            throw new IllegalArgumentException("You can only restore versions of your own articles");
        }

        ArticleVersion version = articleVersionRepository.findByArticleIdAndVersionNumber(articleId, versionNumber)
                .orElseThrow(() -> new IllegalArgumentException("Version not found"));

        // Create version before restoring
        createArticleVersion(article, authorEmail, "Restored from version " + versionNumber);

        // Restore article from version
        article.setTitle(version.getTitle());
        article.setContent(version.getContent());
        article.setSummary(version.getSummary());
        article.setTags(version.getTags());
        article.setCoverImageUrl(version.getCoverImageUrl());
        article.setSlug(version.getSlug());
        article.setReadTime(calculateReadTime(version.getContent()));

        Article restoredArticle = articleRepository.save(article);
        return mapToArticleResponse(restoredArticle);
    }

    @Override
    @Transactional
    public void deleteArticleVersion(String authorEmail, Long articleId, Integer versionNumber) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));

        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!article.getAuthor().getId().equals(author.getId())) {
            throw new IllegalArgumentException("You can only delete versions of your own articles");
        }

        ArticleVersion version = articleVersionRepository.findByArticleIdAndVersionNumber(articleId, versionNumber)
                .orElseThrow(() -> new IllegalArgumentException("Version not found"));

        articleVersionRepository.delete(version);
    }

    private void createArticleVersion(Article article, String editorEmail, String changeDescription) {
        Integer nextVersionNumber = articleVersionRepository.countByArticleId(article.getId()) + 1;
        
        ArticleVersion version = ArticleVersion.builder()
                .article(article)
                .versionNumber(nextVersionNumber)
                .title(article.getTitle())
                .content(article.getContent())
                .summary(article.getSummary())
                .tags(article.getTags())
                .coverImageUrl(article.getCoverImageUrl())
                .slug(article.getSlug())
                .changeDescription(changeDescription)
                .editorEmail(editorEmail)
                .build();

        articleVersionRepository.save(version);
    }

    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }

    private String generateUniqueSlug(String baseSlug) {
        String slug = baseSlug;
        int counter = 1;
        while (articleRepository.findBySlug(slug).isPresent()) {
            slug = baseSlug + "-" + counter++;
        }
        return slug;
    }

    private int calculateReadTime(String content) {
        // Simple calculation: ~200 words per minute
        int wordCount = content.split("\\s+").length;
        return Math.max(1, wordCount / 200);
    }

    private ArticleResponse mapToArticleResponse(Article article) {
        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .summary(article.getSummary())
                .tags(article.getTags())
                .coverImageUrl(article.getCoverImageUrl())
                .slug(article.getSlug())
                .published(article.isPublished())
                .authorUsername(article.getAuthor().getUsername())
                .authorImage(article.getAuthor().getImage())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .publishedAt(article.getPublishedAt())
                .readTime(article.getReadTime())
                .viewCount(article.getViewCount())
                .build();
    }

    private ArticleVersionResponse mapToArticleVersionResponse(ArticleVersion version) {
        return ArticleVersionResponse.builder()
                .id(version.getId())
                .versionNumber(version.getVersionNumber())
                .title(version.getTitle())
                .content(version.getContent())
                .summary(version.getSummary())
                .tags(version.getTags())
                .coverImageUrl(version.getCoverImageUrl())
                .slug(version.getSlug())
                .changeDescription(version.getChangeDescription())
                .editorEmail(version.getEditorEmail())
                .createdAt(version.getCreatedAt())
                .build();
    }
} 