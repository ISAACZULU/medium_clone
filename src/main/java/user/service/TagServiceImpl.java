package user.service;

import com.medium_clone.user.dto.TagResponse;
import com.medium_clone.user.dto.ArticleResponse;
import com.medium_clone.user.entity.Article;
import com.medium_clone.user.entity.Tag;
import user.repository.ArticleRepository;
import user.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;
    private final ArticleRepository articleRepository;

    @Autowired
    public TagServiceImpl(TagRepository tagRepository, ArticleRepository articleRepository) {
        this.tagRepository = tagRepository;
        this.articleRepository = articleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> autocompleteTags(String query) {
        return tagRepository.findTop10ByNameStartingWithIgnoreCaseOrderByUsageCountDesc(query)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getTrendingTags(int limit) {
        return tagRepository.findTrendingTags(PageRequest.of(0, limit))
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleResponse> getArticlesByTag(String tag, int page, int size) {
        // Find articles with the given tag (case-insensitive)
        Set<String> tags = new HashSet<>();
        tags.add(tag.toLowerCase());
        return articleRepository.findByMultipleTags(tags, 1L, PageRequest.of(page, size))
                .stream().map(this::toArticleResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateTagsForArticle(Long articleId, Set<String> tags) {
        if (tags == null) return;
        Set<String> normalized = tags.stream().map(String::toLowerCase).map(String::trim).collect(Collectors.toSet());
        for (String tagName : normalized) {
            Tag tag = tagRepository.findByNameIgnoreCase(tagName).orElse(null);
            if (tag == null) {
                tag = Tag.builder()
                        .name(tagName)
                        .usageCount(1L)
                        .lastUsedAt(LocalDateTime.now())
                        .build();
            } else {
                tag.setUsageCount(tag.getUsageCount() + 1);
                tag.setLastUsedAt(LocalDateTime.now());
            }
            tagRepository.save(tag);
        }
    }

    private TagResponse toResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .usageCount(tag.getUsageCount())
                .lastUsedAt(tag.getLastUsedAt())
                .build();
    }

    private ArticleResponse toArticleResponse(Article article) {
        // Minimal mapping for recommendations; expand as needed
        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .summary(article.getSummary())
                .tags(article.getTags())
                .coverImageUrl(article.getCoverImageUrl())
                .slug(article.getSlug())
                .published(article.isPublished())
                .authorUsername(article.getAuthor().getUsername())
                .createdAt(article.getCreatedAt())
                .publishedAt(article.getPublishedAt())
                .build();
    }
} 