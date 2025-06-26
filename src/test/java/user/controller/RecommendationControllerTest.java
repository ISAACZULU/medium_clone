package user.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import user.service.RecommendationService;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecommendationController.class)
class RecommendationControllerTest {

    @Mock
    private RecommendationService recommendationService;

    @InjectMocks
    private RecommendationController recommendationController;

    @Mock
    private MockMvc mockMvc;

    @Test
    void shouldGenerateRecommendations() throws Exception {
        List<Long> recommendedArticleIds = List.of(1L, 2L, 3L);

        when(recommendationService.generateRecommendationsForAllUsers()).thenReturn("Recommendations generated successfully");

        mockMvc.perform(post("/api/recommendations/generate-all")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Recommendations generated successfully"));
    }

    @Test
    void shouldGetRecommendedArticleIds() throws Exception {
        List<Long> recommendedArticleIds = List.of(1L, 2L, 3L);

        when(recommendationService.getRecommendedArticleIds(anyString(), anyInt())).thenReturn(recommendedArticleIds);

        mockMvc.perform(get("/api/recommendations/ids")
                .header("Authorization", "Bearer test-token")
                .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void shouldGenerateRecommendationsForAllUsers() throws Exception {
        when(recommendationService.generateRecommendationsForAllUsers()).thenReturn("Recommendations generated for all users");

        mockMvc.perform(post("/api/recommendations/generate-all")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Recommendations generated for all users"));
    }
}
