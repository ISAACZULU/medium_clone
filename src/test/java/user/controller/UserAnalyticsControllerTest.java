package user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import user.dto.UserAnalyticsResponse;
import user.dto.ReadingHistoryResponse;
import user.dto.EngagementMetricsResponse;
import user.service.UserAnalyticsService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserAnalyticsController.class)
class UserAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserAnalyticsService userAnalyticsService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserAnalyticsResponse analyticsResponse;
    private ReadingHistoryResponse readingHistoryResponse;
    private EngagementMetricsResponse engagementMetricsResponse;

    @BeforeEach
    void setUp() {
        analyticsResponse = UserAnalyticsResponse.builder()
                .userId(1L)
                .username("testuser")
                .userEmail("test@example.com")
                .analyticsDate(LocalDateTime.now())
                .timeRange("today")
                .build();

        readingHistoryResponse = ReadingHistoryResponse.builder()
                .userId(1L)
                .username("testuser")
                .timeRange("week")
                .generatedAt(LocalDateTime.now())
                .build();

        engagementMetricsResponse = EngagementMetricsResponse.builder()
                .userId(1L)
                .username("testuser")
                .userEmail("test@example.com")
                .metricsDate(LocalDateTime.now())
                .timeRange("week")
                .build();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getUserAnalytics_ShouldReturnAnalytics() throws Exception {
        when(userAnalyticsService.getUserAnalytics(anyString(), anyString())).thenReturn(analyticsResponse);
        mockMvc.perform(get("/analytics/user")
                        .param("timeRange", "today")
                        .header("Authorization", "Bearer test.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getUserAnalyticsByDate_ShouldReturnAnalytics() throws Exception {
        when(userAnalyticsService.getUserAnalyticsByDate(anyString(), any(LocalDateTime.class))).thenReturn(analyticsResponse);
        mockMvc.perform(get("/analytics/user/date")
                        .param("date", LocalDateTime.now().toString())
                        .header("Authorization", "Bearer test.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getUserAnalyticsByDateRange_ShouldReturnAnalytics() throws Exception {
        when(userAnalyticsService.getUserAnalyticsByDateRange(anyString(), any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(analyticsResponse);
        mockMvc.perform(get("/analytics/user/range")
                        .param("startDate", LocalDateTime.now().minusDays(7).toString())
                        .param("endDate", LocalDateTime.now().toString())
                        .header("Authorization", "Bearer test.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getReadingHistory_ShouldReturnHistory() throws Exception {
        when(userAnalyticsService.getReadingHistory(anyString(), anyString())).thenReturn(readingHistoryResponse);
        mockMvc.perform(get("/analytics/reading-history")
                        .param("timeRange", "week")
                        .header("Authorization", "Bearer test.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getRecentReads_ShouldReturnRecentReads() throws Exception {
        when(userAnalyticsService.getRecentReads(anyString(), anyInt())).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/analytics/reading-history/recent")
                        .param("limit", "5")
                        .header("Authorization", "Bearer test.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getEngagementMetrics_ShouldReturnMetrics() throws Exception {
        when(userAnalyticsService.getEngagementMetrics(anyString(), anyString())).thenReturn(engagementMetricsResponse);
        mockMvc.perform(get("/analytics/engagement")
                        .param("timeRange", "week")
                        .header("Authorization", "Bearer test.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPlatformAnalytics_ShouldReturnPlatformStats() throws Exception {
        when(userAnalyticsService.getPlatformAnalytics(anyString())).thenReturn(Map.of("today", List.of(), "week", List.of()));
        mockMvc.perform(get("/analytics/admin/platform")
                        .param("timeRange", "week"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.today").exists())
                .andExpect(jsonPath("$.week").exists());
    }

    @Test
    void getUserAnalytics_Unauthorized_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/analytics/user")
                        .param("timeRange", "today"))
                .andExpect(status().isUnauthorized());
    }
}
