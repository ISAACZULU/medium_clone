package user.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import user.service.ArticleService;
import user.dto.CreateArticleRequest;
import user.dto.ArticleResponse;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArticleController.class)
class ArticleControllerTest {

    @Mock
    private ArticleService articleService;

    @InjectMocks
    private ArticleController articleController;

    @Mock
    private MockMvc mockMvc;

    private CreateArticleRequest testArticle;

    @BeforeEach
    void setUp() {
        testArticle = new CreateArticleRequest();
        testArticle.setTitle("Test Article");
        testArticle.setContent("Test content");
        testArticle.setTags(List.of("test", "article"));
    }

    @Test
    void shouldCreateArticle() throws Exception {
        ArticleResponse expectedResponse = new ArticleResponse();
        expectedResponse.setId(1L);
        expectedResponse.setTitle("Test Article");

        when(articleService.createArticle(anyString(), any(CreateArticleRequest.class))).thenReturn(expectedResponse);

        mockMvc.perform(post("/api/articles")
                .header("Authorization", "Bearer test-token")
                .contentType("application/json")
                .content("{\"title\":\"Test Article\",\"content\":\"Test content\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(1L))
                .andExpect(jsonPath("title").value("Test Article"));
    }

    @Test
    void shouldUpdateArticle() throws Exception {
        ArticleResponse expectedResponse = new ArticleResponse();
        expectedResponse.setId(1L);
        expectedResponse.setTitle("Updated Article");

        when(articleService.updateArticle(anyString(), anyLong(), any(CreateArticleRequest.class))).thenReturn(expectedResponse);

        mockMvc.perform(put("/api/articles/1")
                .header("Authorization", "Bearer test-token")
                .contentType("application/json")
                .content("{\"title\":\"Updated Article\",\"content\":\"Updated content\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title").value("Updated Article"));
    }

    @Test
    void shouldGetArticleVersions() throws Exception {
        List<ArticleResponse> versions = List.of(
            new ArticleResponse(),
            new ArticleResponse()
        );

        when(articleService.getArticleVersions(anyLong())).thenReturn(versions);

        mockMvc.perform(get("/api/articles/1/versions")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldPublishArticle() throws Exception {
        ArticleResponse expectedResponse = new ArticleResponse();
        expectedResponse.setId(1L);
        expectedResponse.setTitle("Test Article");

        when(articleService.publishArticle(anyString(), anyLong())).thenReturn(expectedResponse);

        mockMvc.perform(post("/api/articles/1/publish")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(1L));
    }

    @Test
    void shouldUnpublishArticle() throws Exception {
        mockMvc.perform(post("/api/articles/1/unpublish")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Article unpublished successfully"));
    }
}
