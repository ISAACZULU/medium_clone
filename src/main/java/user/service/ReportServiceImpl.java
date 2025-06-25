package user.service;

import user.dto.ReportRequest;
import user.dto.ReportResponse;
import user.entity.Report;
import user.entity.User;
import user.entity.Article;
import user.entity.Comment;
import user.repository.ReportRepository;
import user.repository.UserRepository;
import user.repository.ArticleRepository;
import user.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public ReportServiceImpl(ReportRepository reportRepository, UserRepository userRepository, 
                           ArticleRepository articleRepository, CommentRepository commentRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.articleRepository = articleRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    @Transactional
    public ReportResponse submitReport(String reporterEmail, ReportRequest request) {
        User reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Validate that the reported content exists
        validateReportedContent(request.getContentType(), request.getReportedContentId());

        // Check if user already reported this content
        List<Report> existingReports = reportRepository.findByReportedContentIdAndContentType(
                request.getReportedContentId(), request.getContentType());
        if (existingReports.stream().anyMatch(r -> r.getReporter().getId().equals(reporter.getId()))) {
            throw new IllegalArgumentException("You have already reported this content");
        }

        Report report = Report.builder()
                .reporter(reporter)
                .contentType(request.getContentType())
                .reportedContentId(request.getReportedContentId())
                .reason(request.getReason())
                .status(Report.ReportStatus.PENDING)
                .build();

        Report saved = reportRepository.save(report);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getUserReports(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        return reportRepository.findByReporterIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getPendingReports() {
        return reportRepository.findByStatusOrderByCreatedAtDesc(Report.ReportStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getReportsByStatus(Report.ReportStatus status) {
        return reportRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getReportsByContentType(Report.ContentType contentType) {
        return reportRepository.findByContentTypeAndStatusOrderByCreatedAtDesc(contentType, Report.ReportStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReportResponse updateReportStatus(Long reportId, Report.ReportStatus status, String moderatorEmail, String notes) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        
        User moderator = userRepository.findByEmail(moderatorEmail)
                .orElseThrow(() -> new IllegalArgumentException("Moderator not found"));

        if (!moderator.isAdmin()) {
            throw new IllegalArgumentException("Only admins can update report status");
        }

        report.setStatus(status);
        report.setModerator(moderator);
        report.setModeratorNotes(notes);
        
        if (status == Report.ReportStatus.RESOLVED || status == Report.ReportStatus.DISMISSED) {
            report.setResolvedAt(LocalDateTime.now());
        }

        Report saved = reportRepository.save(report);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ReportResponse assignReportToModerator(Long reportId, String moderatorEmail) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        
        User moderator = userRepository.findByEmail(moderatorEmail)
                .orElseThrow(() -> new IllegalArgumentException("Moderator not found"));

        if (!moderator.isAdmin()) {
            throw new IllegalArgumentException("Only admins can be assigned as moderators");
        }

        report.setStatus(Report.ReportStatus.UNDER_REVIEW);
        report.setModerator(moderator);

        Report saved = reportRepository.save(report);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void dismissReport(Long reportId, String moderatorEmail, String reason) {
        updateReportStatus(reportId, Report.ReportStatus.DISMISSED, moderatorEmail, reason);
    }

    @Override
    @Transactional
    public void resolveReport(Long reportId, String moderatorEmail, String action) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        // Take action based on the report
        if (report.getContentType() == Report.ContentType.ARTICLE) {
            Article article = articleRepository.findById(report.getReportedContentId())
                    .orElseThrow(() -> new IllegalArgumentException("Article not found"));
            
            if ("remove".equals(action)) {
                article.setPublished(false);
                articleRepository.save(article);
            }
        } else if (report.getContentType() == Report.ContentType.COMMENT) {
            Comment comment = commentRepository.findById(report.getReportedContentId())
                    .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
            
            if ("remove".equals(action)) {
                comment.setDeleted(true);
                commentRepository.save(comment);
            }
        }

        updateReportStatus(reportId, Report.ReportStatus.RESOLVED, moderatorEmail, "Action taken: " + action);
    }

    private void validateReportedContent(Report.ContentType contentType, Long contentId) {
        if (contentType == Report.ContentType.ARTICLE) {
            if (!articleRepository.existsById(contentId)) {
                throw new IllegalArgumentException("Article not found");
            }
        } else if (contentType == Report.ContentType.COMMENT) {
            if (!commentRepository.existsById(contentId)) {
                throw new IllegalArgumentException("Comment not found");
            }
        }
    }

    private ReportResponse toResponse(Report report) {
        ReportResponse response = new ReportResponse();
        response.setId(report.getId());
        response.setReporterUsername(report.getReporter().getUsername());
        response.setContentType(report.getContentType().name());
        response.setReportedContentId(report.getReportedContentId());
        response.setReason(report.getReason());
        response.setStatus(report.getStatus().name());
        response.setModeratorNotes(report.getModeratorNotes());
        response.setCreatedAt(report.getCreatedAt());
        response.setResolvedAt(report.getResolvedAt());

        if (report.getModerator() != null) {
            response.setModeratorUsername(report.getModerator().getUsername());
        }

        // Get content title/preview
        if (report.getContentType() == Report.ContentType.ARTICLE) {
            articleRepository.findById(report.getReportedContentId()).ifPresent(article -> {
                response.setReportedContentTitle(article.getTitle());
            });
        } else if (report.getContentType() == Report.ContentType.COMMENT) {
            commentRepository.findById(report.getReportedContentId()).ifPresent(comment -> {
                String preview = comment.getContent().length() > 50 ? 
                    comment.getContent().substring(0, 50) + "..." : comment.getContent();
                response.setReportedContentTitle(preview);
            });
        }

        return response;
    }
} 