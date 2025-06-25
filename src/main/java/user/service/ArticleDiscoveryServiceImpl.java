package user.service;

import com.medium_clone.user.dto.ArticleResponse;
import com.medium_clone.user.dto.ArticleSearchRequest;
import com.medium_clone.user.dto.PersonalizedFeedRequest;
import com.medium_clone.user.entity.Article;
import com.medium_clone.user.entity.ArticleEngagement;
import com.medium_clone.user.entity.User;
import user.repository.ArticleEngagementRepository;
import user.repository.ArticleRepository;
import user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArticleDiscoveryServiceImpl implements ArticleDiscoveryService {

    private final ArticleRepository articleRepository;
    private final ArticleEngagementRepository engagementRepository;
    private final UserRepository userRepository;

    @Autowired
    public ArticleDiscoveryServiceImpl(ArticleRepository articleRepository,
                                     ArticleEngagementRepository engagementRepository,
                                     UserRepository userRepository) {
        this.articleRepository = articleRepository;
        this.engagementRepository = engagementRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleResponse> getPersonalizedFeed(String userEmail, PersonalizedFeedRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Pageable pageable = createPageable(request.getSortBy(), request.getSortOrder(), request.getPage(), request.getSize());
        
        List<Article> personalizedArticles = new ArrayList<>();

        // Get articles from followed authors
        if (request.isIncludeFollowedAuthors()) {
            Page<Article> followedAuthorArticles = articleRepository.findByAuthorUsernameIn(
                user.getFollowing().stream().map(User::getUsername).collect(Collectors.toList()),
                pageable
            );
            personalizedArticles.addAll(followedAuthorArticles.getContent());
        }

        // Get articles with followed tags
        if (request.isIncludeFollowedTags()) {
            // This would need a more complex query to get articles with tags that user follows
            // For now, we'll get articles with popular tags
            Page<Article> taggedArticles = articleRepository.findByPublishedTrue(pageable);
            personalizedArticles.addAll(taggedArticles.getContent());
        }

        // Include trending articles
        if (request.isIncludeTrending()) {
            Page<Article> trendingArticles = getTrendingArticles(request.getPage(), request.getSize());
            personalizedArticles.addAll(trendingArticles.getContent());
        }

        // Remove duplicates and sort
        List<Article> uniqueArticles = personalizedArticles.stream()
                .distinct()
                .sorted(Comparator.comparing(Article::getPublishedAt).reversed())
                .collect(Collectors.toList());

        // Convert to Page<ArticleResponse>
        return createArticleResponsePage(uniqueArticles, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleResponse> getTrendingArticles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "viewCount", "publishedAt"));
        Page<Article> trendingArticles = articleRepository.findTrendingByViews(pageable);
        return trendingArticles.map(this::mapToArticleResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleResponse> advancedSearch(ArticleSearchRequest request) {
        Pageable pageable = createPageable(request.getSortBy(), request.getSortOrder(), request.getPage(), request.getSize());
        
        Page<Article> articles = articleRepository.advancedSearch(
            request.getKeywords(),
            request.getAuthorUsername(),
            request.getFromDate(),
            request.getToDate(),
            request.isPublishedOnly(),
            pageable
        );
        
        return articles.map(this::mapToArticleResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleResponse> searchByTags(Set<String> tags, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<Article> articles = articleRepository.findByMultipleTags(tags, (long) tags.size(), pageable);
        return articles.map(this::mapToArticleResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ArticleResponse> getRecentArticles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        Page<Article> recentArticles = articleRepository.findRecentArticles(pageable);
        return recentArticles.map(this::mapToArticleResponse);
    }

    @Override
    @Transactional
    public void trackEngagement(String userEmail, Long articleId, String engagementType) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));

        ArticleEngagement.EngagementType type;
        try {
            type = ArticleEngagement.EngagementType.valueOf(engagementType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid engagement type: " + engagementType);
        }

        if (type == ArticleEngagement.EngagementType.CLAP) {
            // Medium-style claps: increment up to 50 per user per article
            Optional<ArticleEngagement> existingClap = engagementRepository
                    .findByArticleIdAndUserIdAndType(articleId, user.getId(), type);
            int maxClaps = 50;
            if (existingClap.isPresent()) {
                ArticleEngagement engagement = existingClap.get();
                int current = engagement.getCount() != null ? engagement.getCount() : 0;
                if (current < maxClaps) {
                    engagement.setCount(Math.min(current + 1, maxClaps));
                    engagement.setCreatedAt(LocalDateTime.now());
                    engagementRepository.save(engagement);
                }
            } else {
                ArticleEngagement engagement = ArticleEngagement.builder()
                        .article(article)
                        .user(user)
                        .type(type)
                        .count(1)
                        .build();
                engagementRepository.save(engagement);
            }
            return;
        }

        // Check if engagement already exists (for other types)
        Optional<ArticleEngagement> existingEngagement = engagementRepository
                .findByArticleIdAndUserIdAndType(articleId, user.getId(), type);

        if (existingEngagement.isPresent()) {
            // Update existing engagement timestamp
            ArticleEngagement engagement = existingEngagement.get();
            engagement.setCreatedAt(LocalDateTime.now());
            engagementRepository.save(engagement);
        } else {
            // Create new engagement
            ArticleEngagement engagement = ArticleEngagement.builder()
                    .article(article)
                    .user(user)
                    .type(type)
                    .build();
            engagementRepository.save(engagement);
        }

        // Update article view count if it's a view
        if (type == ArticleEngagement.EngagementType.VIEW) {
            article.setViewCount(article.getViewCount() + 1);
            articleRepository.save(article);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getArticleEngagementStats(Long articleId) {
        Map<String, Long> stats = new HashMap<>();
        
        for (ArticleEngagement.EngagementType type : ArticleEngagement.EngagementType.values()) {
            Long count = engagementRepository.countByArticleIdAndType(articleId, type);
            stats.put(type.name().toLowerCase(), count);
        }
        
        return stats;
    }

    public Long getTotalClaps(Long articleId) {
        return engagementRepository.sumClapsByArticleId(articleId);
    }

    private Pageable createPageable(String sortBy, String sortOrder, int page, int size) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? 
            Sort.Direction.ASC : Sort.Direction.DESC;
        
        Sort sort = Sort.by(direction, sortBy);
        return PageRequest.of(page, size, sort);
    }

    private Page<ArticleResponse> createArticleResponsePage(List<Article> articles, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), articles.size());
        
        List<ArticleResponse> content = articles.subList(start, end)
                .stream()
                .map(this::mapToArticleResponse)
                .collect(Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(content, pageable, articles.size());
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
} 