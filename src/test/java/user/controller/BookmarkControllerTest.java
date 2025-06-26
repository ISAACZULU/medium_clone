package user.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import user.service.BookmarkService;
import user.dto.BookmarkRequest;
import user.dto.BookmarkResponse;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookmarkController.class)
class BookmarkControllerTest {

    @Mock
    private BookmarkService bookmarkService;

    @InjectMocks
    private BookmarkController bookmarkController;

    @Mock
    private MockMvc mockMvc;

    private BookmarkRequest testBookmark;

    @BeforeEach
    void setUp() {
        testBookmark = new BookmarkRequest();
        testBookmark.setArticleId(1L);
        testBookmark.setTitle("Test Article");
        testBookmark.setSummary("Test summary");
    }

    @Test
    void shouldAddBookmark() throws Exception {
        BookmarkResponse expectedResponse = new BookmarkResponse();
        expectedResponse.setId(1L);
        expectedResponse.setTitle("Test Article");

        when(bookmarkService.addBookmark(anyString(), any(BookmarkRequest.class))).thenReturn(expectedResponse);

        mockMvc.perform(post("/api/bookmarks")
                .header("Authorization", "Bearer test-token")
                .contentType("application/json")
                .content("{\"articleId\":1,\"title\":\"Test Article\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(1L))
                .andExpect(jsonPath("title").value("Test Article"));
    }

    @Test
    void shouldGetUserBookmarks() throws Exception {
        List<BookmarkResponse> bookmarks = List.of(
            new BookmarkResponse(),
            new BookmarkResponse()
        );

        when(bookmarkService.getUserBookmarks(anyString())).thenReturn(bookmarks);

        mockMvc.perform(get("/api/bookmarks")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldDeleteBookmark() throws Exception {
        mockMvc.perform(delete("/api/bookmarks/1")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Bookmark deleted successfully"));
    }

    @Test
    void shouldGetBookmarkById() throws Exception {
        BookmarkResponse expectedResponse = new BookmarkResponse();
        expectedResponse.setId(1L);
        expectedResponse.setTitle("Test Article");

        when(bookmarkService.getBookmarkById(anyString(), anyLong())).thenReturn(expectedResponse);

        mockMvc.perform(get("/api/bookmarks/1")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(1L))
                .andExpect(jsonPath("title").value("Test Article"));
    }
}
