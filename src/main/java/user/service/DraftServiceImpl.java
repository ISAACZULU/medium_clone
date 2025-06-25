package user.service;

import com.medium_clone.user.dto.CreateArticleRequest;
import com.medium_clone.user.dto.ArticleResponse;
import com.medium_clone.user.entity.Article;
import com.medium_clone.user.entity.User;
import user.repository.ArticleRepository;
import user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DraftServiceImpl implements DraftService {
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Autowired
    public DraftServiceImpl(ArticleRepository articleRepository, UserRepository userRepository) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ArticleResponse autoSaveDraft(String userEmail, CreateArticleRequest request, Long draftId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Article draft;
        if (draftId != null) {
            draft = articleRepository.findById(draftId)
                    .orElseThrow(() -> new IllegalArgumentException("Draft not found"));
            if (!draft.getAuthor().getId().equals(user.getId()) || draft.isPublished()) {
                throw new IllegalArgumentException("You can only auto-save your own drafts");
            }
        } else {
            draft = Article.builder()
                    .author(user)
                    .published(false)
                    .viewCount(0)
                    .build();
        }
        draft.setTitle(request.getTitle());
        draft.setContent(request.getContent());
        draft.setSummary(request.getSummary());
        draft.setTags(request.getTags());
        draft.setCoverImageUrl(request.getCoverImageUrl());
        draft.setReadTime(0);
        draft.setLastSavedAt(LocalDateTime.now());
        Article saved = articleRepository.save(draft);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ArticleResponse updateDraft(String userEmail, Long draftId, CreateArticleRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Article draft = articleRepository.findById(draftId)
                .orElseThrow(() -> new IllegalArgumentException("Draft not found"));
        if (!draft.getAuthor().getId().equals(user.getId()) || draft.isPublished()) {
            throw new IllegalArgumentException("You can only update your own drafts");
        }
        draft.setTitle(request.getTitle());
        draft.setContent(request.getContent());
        draft.setSummary(request.getSummary());
        draft.setTags(request.getTags());
        draft.setCoverImageUrl(request.getCoverImageUrl());
        draft.setReadTime(0);
        draft.setLastSavedAt(LocalDateTime.now());
        Article saved = articleRepository.save(draft);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleResponse> listDrafts(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return articleRepository.findByAuthorIdAndPublishedTrue(user.getId()).stream()
                .filter(a -> !a.isPublished())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleResponse getDraft(String userEmail, Long draftId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Article draft = articleRepository.findById(draftId)
                .orElseThrow(() -> new IllegalArgumentException("Draft not found"));
        if (!draft.getAuthor().getId().equals(user.getId()) || draft.isPublished()) {
            throw new IllegalArgumentException("You can only view your own drafts");
        }
        return toResponse(draft);
    }

    @Override
    @Transactional
    public void deleteDraft(String userEmail, Long draftId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Article draft = articleRepository.findById(draftId)
                .orElseThrow(() -> new IllegalArgumentException("Draft not found"));
        if (!draft.getAuthor().getId().equals(user.getId()) || draft.isPublished()) {
            throw new IllegalArgumentException("You can only delete your own drafts");
        }
        articleRepository.delete(draft);
    }

    @Override
    @Transactional
    public ArticleResponse publishDraft(String userEmail, Long draftId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Article draft = articleRepository.findById(draftId)
                .orElseThrow(() -> new IllegalArgumentException("Draft not found"));
        if (!draft.getAuthor().getId().equals(user.getId()) || draft.isPublished()) {
            throw new IllegalArgumentException("You can only publish your own drafts");
        }
        draft.setPublished(true);
        draft.setPublishedAt(LocalDateTime.now());
        Article saved = articleRepository.save(draft);
        return toResponse(saved);
    }

    private ArticleResponse toResponse(Article article) {
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