package user.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import user.service.ReportService;
import user.dto.ReportRequest;
import user.dto.ReportResponse;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    @Mock
    private MockMvc mockMvc;

    private ReportRequest testReport;

    @BeforeEach
    void setUp() {
        testReport = new ReportRequest();
        testReport.setArticleId(1L);
        testReport.setReason("Inappropriate content");
        testReport.setDescription("This article contains inappropriate content");
    }

    @Test
    void shouldSubmitReport() throws Exception {
        ReportResponse expectedResponse = new ReportResponse();
        expectedResponse.setId(1L);
        expectedResponse.setStatus("PENDING");

        when(reportService.submitReport(any(ReportRequest.class), anyString())).thenReturn(expectedResponse);

        mockMvc.perform(post("/api/reports")
                .header("Authorization", "Bearer test-token")
                .contentType("application/json")
                .content("{\"articleId\":1,\"reason\":\"Inappropriate content\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(1L))
                .andExpect(jsonPath("status").value("PENDING"));
    }

    @Test
    void shouldGetPendingReports() throws Exception {
        List<ReportResponse> reports = List.of(
            new ReportResponse(),
            new ReportResponse()
        );

        when(reportService.getPendingReports()).thenReturn(reports);

        mockMvc.perform(get("/api/reports/pending")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldUpdateReportStatus() throws Exception {
        ReportResponse expectedResponse = new ReportResponse();
        expectedResponse.setId(1L);
        expectedResponse.setStatus("RESOLVED");

        when(reportService.updateReportStatus(anyLong(), any(), anyString(), anyString()))
                .thenReturn(expectedResponse);

        mockMvc.perform(put("/api/reports/1/status")
                .header("Authorization", "Bearer test-token")
                .contentType("application/json")
                .content("{\"status\":\"RESOLVED\",\"notes\":\"Resolved inappropriate content\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("RESOLVED"));
    }

    @Test
    void shouldGetReportsByStatus() throws Exception {
        List<ReportResponse> reports = List.of(
            new ReportResponse(),
            new ReportResponse()
        );

        when(reportService.getReportsByStatus(any())).thenReturn(reports);

        mockMvc.perform(get("/api/reports/status/RESOLVED")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
