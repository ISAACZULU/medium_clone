package user.service;

import com.medium_clone.user.dto.CreateArticleRequest;
import com.medium_clone.user.dto.ArticleResponse;
import java.util.List;

public interface DraftService {
    ArticleResponse autoSaveDraft(String userEmail, CreateArticleRequest request, Long draftId);
    ArticleResponse updateDraft(String userEmail, Long draftId, CreateArticleRequest request);
    List<ArticleResponse> listDrafts(String userEmail);
    ArticleResponse getDraft(String userEmail, Long draftId);
    void deleteDraft(String userEmail, Long draftId);
    ArticleResponse publishDraft(String userEmail, Long draftId);
} 