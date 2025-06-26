package user.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import user.service.CacheManagementService;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CacheManagementController.class)
class CacheManagementControllerTest {

    @Mock
    private CacheManagementService cacheManagementService;

    @InjectMocks
    private CacheManagementController cacheManagementController;

    @Mock
    private MockMvc mockMvc;

    @Test
    void shouldGetCacheStatistics() throws Exception {
        Map<String, Object> stats = Map.of(
            "hitRate", 0.95,
            "missRate", 0.05,
            "size", 1000L,
            "evictions", 50L
        );

        when(cacheManagementService.getCacheStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/cache/stats")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("hitRate").value(0.95))
                .andExpect(jsonPath("missRate").value(0.05))
                .andExpect(jsonPath("size").value(1000L))
                .andExpect(jsonPath("evictions").value(50L));
    }

    @Test
    void shouldClearAllCaches() throws Exception {
        mockMvc.perform(delete("/api/v1/cache/clear-all")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("All caches cleared successfully"))
                .andExpect(jsonPath("status").value("success"));
    }

    @Test
    void shouldClearArticleCaches() throws Exception {
        mockMvc.perform(delete("/api/v1/cache/clear/articles")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("Article caches cleared successfully"))
                .andExpect(jsonPath("status").value("success"));
    }

    @Test
    void shouldClearDraftCaches() throws Exception {
        mockMvc.perform(delete("/api/v1/cache/clear/drafts")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("Draft caches cleared successfully"))
                .andExpect(jsonPath("status").value("success"));
    }

    @Test
    void shouldClearCommentCaches() throws Exception {
        mockMvc.perform(delete("/api/v1/cache/clear/comments")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("message").value("Comment caches cleared successfully"))
                .andExpect(jsonPath("status").value("success"));
    }
}
