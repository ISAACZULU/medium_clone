package user.controller;

import user.dto.ReportRequest;
import user.dto.ReportResponse;
import user.entity.Report;
import user.service.ReportService;
import user.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;
    private final JwtUtil jwtUtil;

    @Autowired
    public ReportController(ReportService reportService, JwtUtil jwtUtil) {
        this.reportService = reportService;
        this.jwtUtil = jwtUtil;
    }

    // User endpoints
    @PostMapping
    public ReportResponse submitReport(@RequestBody ReportRequest request, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return reportService.submitReport(email, request);
    }

    @GetMapping("/my-reports")
    public List<ReportResponse> getUserReports(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return reportService.getUserReports(email);
    }

    // Admin/Moderator endpoints
    @GetMapping("/pending")
    public List<ReportResponse> getPendingReports(@RequestHeader("Authorization") String authHeader) {
        validateAdmin(authHeader);
        return reportService.getPendingReports();
    }

    @GetMapping("/status/{status}")
    public List<ReportResponse> getReportsByStatus(@PathVariable String status, @RequestHeader("Authorization") String authHeader) {
        validateAdmin(authHeader);
        Report.ReportStatus reportStatus = Report.ReportStatus.valueOf(status.toUpperCase());
        return reportService.getReportsByStatus(reportStatus);
    }

    @GetMapping("/content-type/{contentType}")
    public List<ReportResponse> getReportsByContentType(@PathVariable String contentType, @RequestHeader("Authorization") String authHeader) {
        validateAdmin(authHeader);
        Report.ContentType reportContentType = Report.ContentType.valueOf(contentType.toUpperCase());
        return reportService.getReportsByContentType(reportContentType);
    }

    @PutMapping("/{reportId}/status")
    public ReportResponse updateReportStatus(@PathVariable Long reportId, @RequestBody Map<String, String> request, 
                                           @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        validateAdmin(authHeader);
        
        Report.ReportStatus status = Report.ReportStatus.valueOf(request.get("status").toUpperCase());
        String notes = request.get("notes");
        
        return reportService.updateReportStatus(reportId, status, email, notes);
    }

    @PostMapping("/{reportId}/assign")
    public ReportResponse assignReportToModerator(@PathVariable Long reportId, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        validateAdmin(authHeader);
        
        return reportService.assignReportToModerator(reportId, email);
    }

    @PostMapping("/{reportId}/dismiss")
    public ResponseEntity<String> dismissReport(@PathVariable Long reportId, @RequestBody Map<String, String> request, 
                                              @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        validateAdmin(authHeader);
        
        String reason = request.get("reason");
        reportService.dismissReport(reportId, email, reason);
        return ResponseEntity.ok("Report dismissed successfully");
    }

    @PostMapping("/{reportId}/resolve")
    public ResponseEntity<String> resolveReport(@PathVariable Long reportId, @RequestBody Map<String, String> request, 
                                              @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        validateAdmin(authHeader);
        
        String action = request.get("action"); // "remove", "warn", etc.
        reportService.resolveReport(reportId, email, action);
        return ResponseEntity.ok("Report resolved successfully");
    }

    private void validateAdmin(String authHeader) {
        // This would typically check if the user has admin role
        // For now, we'll assume the security config handles this
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