package user.service;

import com.medium_clone.user.dto.TagResponse;
import com.medium_clone.user.dto.ArticleResponse;
import java.util.List;

public interface TagService {
    List<TagResponse> autocompleteTags(String query);
    List<TagResponse> getTrendingTags(int limit);
    List<ArticleResponse> getArticlesByTag(String tag, int page, int size);
    void updateTagsForArticle(Long articleId, java.util.Set<String> tags);
} 