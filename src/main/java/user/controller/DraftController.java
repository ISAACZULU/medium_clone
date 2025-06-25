package user.controller;

import com.medium_clone.user.dto.CreateArticleRequest;
import com.medium_clone.user.dto.ArticleResponse;
import user.service.DraftService;
import user.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drafts")
public class DraftController {
    private final DraftService draftService;
    private final JwtUtil jwtUtil;

    @Autowired
    public DraftController(DraftService draftService, JwtUtil jwtUtil) {
        this.draftService = draftService;
        this.jwtUtil = jwtUtil;
    }

    // Auto-save or create draft
    @PostMapping
    public ArticleResponse autoSaveDraft(@Valid @RequestBody CreateArticleRequest request,
                                         @RequestParam(required = false) Long draftId,
                                         @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return draftService.autoSaveDraft(email, request, draftId);
    }

    // Update draft
    @PutMapping("/{draftId}")
    public ArticleResponse updateDraft(@PathVariable Long draftId,
                                       @Valid @RequestBody CreateArticleRequest request,
                                       @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return draftService.updateDraft(email, draftId, request);
    }

    // List drafts for user
    @GetMapping
    public List<ArticleResponse> listDrafts(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return draftService.listDrafts(email);
    }

    // Get a specific draft
    @GetMapping("/{draftId}")
    public ArticleResponse getDraft(@PathVariable Long draftId,
                                    @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return draftService.getDraft(email, draftId);
    }

    // Delete draft
    @DeleteMapping("/{draftId}")
    public ResponseEntity<String> deleteDraft(@PathVariable Long draftId,
                                              @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        draftService.deleteDraft(email, draftId);
        return ResponseEntity.ok("Draft deleted");
    }

    // Publish draft
    @PostMapping("/{draftId}/publish")
    public ArticleResponse publishDraft(@PathVariable Long draftId,
                                        @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return draftService.publishDraft(email, draftId);
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