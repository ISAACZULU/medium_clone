package user.service;

import com.medium_clone.user.dto.*;
import com.medium_clone.user.entity.*;
import user.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookmarkServiceImpl implements BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final BookmarkCollectionRepository collectionRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Autowired
    public BookmarkServiceImpl(BookmarkRepository bookmarkRepository,
                              BookmarkCollectionRepository collectionRepository,
                              ArticleRepository articleRepository,
                              UserRepository userRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.collectionRepository = collectionRepository;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public BookmarkResponse addBookmark(String userEmail, BookmarkRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Article article = articleRepository.findById(request.getArticleId())
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));
        BookmarkCollection collection = null;
        if (request.getCollectionId() != null) {
            collection = collectionRepository.findById(request.getCollectionId())
                    .orElseThrow(() -> new IllegalArgumentException("Collection not found"));
            if (!collection.getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("You can only add to your own collections");
            }
        }
        if (bookmarkRepository.findByUserAndArticleAndCollection(user, article, collection).isPresent()) {
            throw new IllegalArgumentException("Bookmark already exists");
        }
        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .article(article)
                .collection(collection)
                .build();
        Bookmark saved = bookmarkRepository.save(bookmark);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void removeBookmark(String userEmail, Long bookmarkId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new IllegalArgumentException("Bookmark not found"));
        if (!bookmark.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only remove your own bookmarks");
        }
        bookmarkRepository.delete(bookmark);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkResponse> getBookmarksForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return bookmarkRepository.findByUser(user).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkResponse> getBookmarksInCollection(String userEmail, Long collectionId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        BookmarkCollection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));
        if (!collection.getUser().getId().equals(user.getId()) && !collection.isPublic()) {
            throw new IllegalArgumentException("You can only view your own or public collections");
        }
        return bookmarkRepository.findByUserAndCollection(user, collection).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookmarkCollectionResponse createCollection(String userEmail, BookmarkCollectionRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        BookmarkCollection collection = BookmarkCollection.builder()
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .isPublic(request.isPublic())
                .build();
        BookmarkCollection saved = collectionRepository.save(collection);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public BookmarkCollectionResponse updateCollection(String userEmail, Long collectionId, BookmarkCollectionRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        BookmarkCollection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));
        if (!collection.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only update your own collections");
        }
        collection.setName(request.getName());
        collection.setDescription(request.getDescription());
        collection.setPublic(request.isPublic());
        BookmarkCollection saved = collectionRepository.save(collection);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCollection(String userEmail, Long collectionId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        BookmarkCollection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new IllegalArgumentException("Collection not found"));
        if (!collection.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only delete your own collections");
        }
        collectionRepository.delete(collection);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkCollectionResponse> getCollectionsForUser(String userEmail, boolean onlyPublic) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<BookmarkCollection> collections = onlyPublic ?
                collectionRepository.findByUserAndIsPublicTrue(user) :
                collectionRepository.findByUser(user);
        return collections.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkCollectionResponse> getPublicCollections() {
        return collectionRepository.findByIsPublicTrue().stream().map(this::toResponse).collect(Collectors.toList());
    }

    private BookmarkResponse toResponse(Bookmark bookmark) {
        return BookmarkResponse.builder()
                .id(bookmark.getId())
                .articleId(bookmark.getArticle().getId())
                .collectionId(bookmark.getCollection() != null ? bookmark.getCollection().getId() : null)
                .createdAt(bookmark.getCreatedAt())
                .build();
    }

    private BookmarkCollectionResponse toResponse(BookmarkCollection collection) {
        return BookmarkCollectionResponse.builder()
                .id(collection.getId())
                .name(collection.getName())
                .description(collection.getDescription())
                .isPublic(collection.isPublic())
                .createdAt(collection.getCreatedAt())
                .updatedAt(collection.getUpdatedAt())
                .bookmarkCount(collection.getBookmarks() != null ? collection.getBookmarks().size() : 0)
                .build();
    }
} 