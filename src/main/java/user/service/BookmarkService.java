package user.service;

import com.medium_clone.user.dto.*;
import java.util.List;

public interface BookmarkService {
    BookmarkResponse addBookmark(String userEmail, BookmarkRequest request);
    void removeBookmark(String userEmail, Long bookmarkId);
    List<BookmarkResponse> getBookmarksForUser(String userEmail);
    List<BookmarkResponse> getBookmarksInCollection(String userEmail, Long collectionId);

    BookmarkCollectionResponse createCollection(String userEmail, BookmarkCollectionRequest request);
    BookmarkCollectionResponse updateCollection(String userEmail, Long collectionId, BookmarkCollectionRequest request);
    void deleteCollection(String userEmail, Long collectionId);
    List<BookmarkCollectionResponse> getCollectionsForUser(String userEmail, boolean onlyPublic);
    List<BookmarkCollectionResponse> getPublicCollections();
} 