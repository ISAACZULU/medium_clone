package user.service;

import com.medium_clone.user.dto.CollectionRequest;
import com.medium_clone.user.dto.CollectionResponse;
import java.util.List;

public interface CollectionService {
    CollectionResponse createCollection(String userEmail, CollectionRequest request);
    CollectionResponse updateCollection(String userEmail, Long collectionId, CollectionRequest request);
    void deleteCollection(String userEmail, Long collectionId);
    CollectionResponse addArticleToCollection(String userEmail, Long collectionId, Long articleId);
    CollectionResponse removeArticleFromCollection(String userEmail, Long collectionId, Long articleId);
    CollectionResponse addCollaborator(String userEmail, Long collectionId, Long collaboratorId);
    CollectionResponse removeCollaborator(String userEmail, Long collectionId, Long collaboratorId);
    List<CollectionResponse> getCollectionsForUser(String userEmail);
    List<CollectionResponse> getCollectionsForCollaborator(String userEmail);
    List<CollectionResponse> getPublicCollections();
} 