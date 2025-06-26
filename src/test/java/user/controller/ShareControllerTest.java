package user.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import user.service.ArticleService;
import user.dto.ArticleResponse;
import java.util.Map;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShareController.class)
class ShareControllerTest {

    @Mock
    private ArticleService articleService;

    @InjectMocks
    private ShareController shareController;

    @Mock
    private MockMvc mockMvc;

    @Test
    void shouldGetShareMetadata() throws Exception {
        ArticleResponse article = new ArticleResponse();
        article.setTitle("Test Article");
        article.setSummary("Test summary");
        article.setCoverImageUrl("https://example.com/image.jpg");
        article.setAuthorUsername("testuser");

        Map<String, Object> openGraph = Map.of(
            "title", "Test Article",
            "description", "Test summary",
            "image", "https://example.com/image.jpg"
        );

        Map<String, Object> twitterCard = Map.of(
            "title", "Test Article",
            "description", "Test summary",
            "image", "https://example.com/image.jpg"
        );

        when(articleService.getArticleBySlug(anyString())).thenReturn(article);

        mockMvc.perform(get("/api/share/test-article")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title").value("Test Article"))
                .andExpect(jsonPath("description").value("Test summary"))
                .andExpect(jsonPath("image").value("https://example.com/image.jpg"))
                .andExpect(jsonPath("author").value("testuser"))
                .andExpect(jsonPath("openGraph.title").value("Test Article"))
                .andExpect(jsonPath("twitterCard.title").value("Test Article"));
    }

    @Test
    void shouldReturnNotFoundForNonExistentArticle() throws Exception {
        when(articleService.getArticleBySlug(anyString())).thenThrow(new IllegalArgumentException("Article not found"));

        mockMvc.perform(get("/api/share/non-existent-article")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("error").value("Article not found"));
    }
}
