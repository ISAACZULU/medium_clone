package user.controller;

import com.medium_clone.user.dto.CollectionRequest;
import com.medium_clone.user.dto.CollectionResponse;
import user.service.CollectionService;
import user.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {
    private final CollectionService collectionService;
    private final JwtUtil jwtUtil;

    @Autowired
    public CollectionController(CollectionService collectionService, JwtUtil jwtUtil) {
        this.collectionService = collectionService;
        this.jwtUtil = jwtUtil;
    }

    // Create collection
    @PostMapping
    public CollectionResponse createCollection(@Valid @RequestBody CollectionRequest request,
                                              @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return collectionService.createCollection(email, request);
    }

    // Update collection
    @PutMapping("/{collectionId}")
    public CollectionResponse updateCollection(@PathVariable Long collectionId,
                                              @Valid @RequestBody CollectionRequest request,
                                              @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return collectionService.updateCollection(email, collectionId, request);
    }

    // Delete collection
    @DeleteMapping("/{collectionId}")
    public ResponseEntity<String> deleteCollection(@PathVariable Long collectionId,
                                                   @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        collectionService.deleteCollection(email, collectionId);
        return ResponseEntity.ok("Collection deleted");
    }

    // Add article to collection
    @PostMapping("/{collectionId}/articles/{articleId}")
    public CollectionResponse addArticleToCollection(@PathVariable Long collectionId,
                                                    @PathVariable Long articleId,
                                                    @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return collectionService.addArticleToCollection(email, collectionId, articleId);
    }

    // Remove article from collection
    @DeleteMapping("/{collectionId}/articles/{articleId}")
    public CollectionResponse removeArticleFromCollection(@PathVariable Long collectionId,
                                                         @PathVariable Long articleId,
                                                         @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return collectionService.removeArticleFromCollection(email, collectionId, articleId);
    }

    // Add collaborator
    @PostMapping("/{collectionId}/collaborators/{collaboratorId}")
    public CollectionResponse addCollaborator(@PathVariable Long collectionId,
                                             @PathVariable Long collaboratorId,
                                             @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return collectionService.addCollaborator(email, collectionId, collaboratorId);
    }

    // Remove collaborator
    @DeleteMapping("/{collectionId}/collaborators/{collaboratorId}")
    public CollectionResponse removeCollaborator(@PathVariable Long collectionId,
                                                @PathVariable Long collaboratorId,
                                                @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return collectionService.removeCollaborator(email, collectionId, collaboratorId);
    }

    // Get all collections for user
    @GetMapping
    public List<CollectionResponse> getCollectionsForUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return collectionService.getCollectionsForUser(email);
    }

    // Get all collections where user is a collaborator
    @GetMapping("/collaborating")
    public List<CollectionResponse> getCollectionsForCollaborator(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return collectionService.getCollectionsForCollaborator(email);
    }

    // Get all public collections
    @GetMapping("/public")
    public List<CollectionResponse> getPublicCollections() {
        return collectionService.getPublicCollections();
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