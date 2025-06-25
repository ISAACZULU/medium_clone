package user.controller;

import com.medium_clone.user.dto.*;
import user.service.BookmarkService;
import user.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {
    private final BookmarkService bookmarkService;
    private final JwtUtil jwtUtil;

    @Autowired
    public BookmarkController(BookmarkService bookmarkService, JwtUtil jwtUtil) {
        this.bookmarkService = bookmarkService;
        this.jwtUtil = jwtUtil;
    }

    // Add bookmark
    @PostMapping
    public BookmarkResponse addBookmark(@Valid @RequestBody BookmarkRequest request,
                                        @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return bookmarkService.addBookmark(email, request);
    }

    // Remove bookmark
    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<String> removeBookmark(@PathVariable Long bookmarkId,
                                                 @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        bookmarkService.removeBookmark(email, bookmarkId);
        return ResponseEntity.ok("Bookmark removed");
    }

    // Get all bookmarks for user
    @GetMapping
    public List<BookmarkResponse> getBookmarksForUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return bookmarkService.getBookmarksForUser(email);
    }

    // Get bookmarks in a collection
    @GetMapping("/collection/{collectionId}")
    public List<BookmarkResponse> getBookmarksInCollection(@PathVariable Long collectionId,
                                                           @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return bookmarkService.getBookmarksInCollection(email, collectionId);
    }

    // Create collection
    @PostMapping("/collections")
    public BookmarkCollectionResponse createCollection(@Valid @RequestBody BookmarkCollectionRequest request,
                                                      @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return bookmarkService.createCollection(email, request);
    }

    // Update collection
    @PutMapping("/collections/{collectionId}")
    public BookmarkCollectionResponse updateCollection(@PathVariable Long collectionId,
                                                      @Valid @RequestBody BookmarkCollectionRequest request,
                                                      @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return bookmarkService.updateCollection(email, collectionId, request);
    }

    // Delete collection
    @DeleteMapping("/collections/{collectionId}")
    public ResponseEntity<String> deleteCollection(@PathVariable Long collectionId,
                                                   @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        bookmarkService.deleteCollection(email, collectionId);
        return ResponseEntity.ok("Collection deleted");
    }

    // Get all collections for user
    @GetMapping("/collections")
    public List<BookmarkCollectionResponse> getCollectionsForUser(@RequestHeader("Authorization") String authHeader,
                                                                 @RequestParam(defaultValue = "false") boolean onlyPublic) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return bookmarkService.getCollectionsForUser(email, onlyPublic);
    }

    // Get all public collections
    @GetMapping("/collections/public")
    public List<BookmarkCollectionResponse> getPublicCollections() {
        return bookmarkService.getPublicCollections();
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