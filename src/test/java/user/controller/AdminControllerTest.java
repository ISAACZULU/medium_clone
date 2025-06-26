package user.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import user.service.AdminService;
import user.dto.User;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    @Mock
    private MockMvc mockMvc;

    @Test
    void shouldGetAllUsers() throws Exception {
        List<User> users = List.of(new User(), new User());

        when(adminService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/admin/users")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldGetUserAnalytics() throws Exception {
        when(adminService.getUserAnalytics()).thenReturn(Map.of(
            "totalUsers", 100L,
            "activeUsers", 50L,
            "newUsers", 20L
        ));

        mockMvc.perform(get("/api/admin/analytics/users")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalUsers").value(100L))
                .andExpect(jsonPath("activeUsers").value(50L))
                .andExpect(jsonPath("newUsers").value(20L));
    }

    @Test
    void shouldGetPlatformAnalytics() throws Exception {
        when(adminService.getPlatformAnalytics()).thenReturn(Map.of(
            "totalArticles", 500L,
            "activeUsers", 1000L,
            "totalComments", 2000L
        ));

        mockMvc.perform(get("/api/admin/analytics/platform")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalArticles").value(500L))
                .andExpect(jsonPath("activeUsers").value(1000L))
                .andExpect(jsonPath("totalComments").value(2000L));
    }

    @Test
    void shouldApproveArticle() throws Exception {
        mockMvc.perform(post("/api/admin/articles/1/approve")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Article approved successfully"));
    }

    @Test
    void shouldRejectArticle() throws Exception {
        mockMvc.perform(post("/api/admin/articles/1/reject")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Article rejected successfully"));
    }
}
