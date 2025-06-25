package user.service;

import com.medium_clone.user.dto.ArticleDisplayResponse;
import com.medium_clone.user.dto.ArticleResponse;
import user.util.ArticleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class ArticleDisplayServiceImpl implements ArticleDisplayService {

    private final ArticleService articleService;
    private final ArticleDiscoveryService discoveryService;

    @Autowired
    public ArticleDisplayServiceImpl(ArticleService articleService, ArticleDiscoveryService discoveryService) {
        this.articleService = articleService;
        this.discoveryService = discoveryService;
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleDisplayResponse getArticleForDisplay(String slug, String userEmail) {
        ArticleResponse article = articleService.getArticleBySlug(slug);
        Map<String, Long> engagementStats = discoveryService.getArticleEngagementStats(article.getId());
        
        // Track view if user is authenticated
        if (userEmail != null && !userEmail.isEmpty()) {
            try {
                discoveryService.trackEngagement(userEmail, article.getId(), "VIEW");
            } catch (Exception e) {
                // Continue without tracking if there's an error
            }
        }

        return buildArticleDisplayResponse(article, engagementStats, userEmail);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleDisplayResponse getArticlePreview(String slug) {
        ArticleResponse article = articleService.getArticleBySlug(slug);
        Map<String, Long> engagementStats = discoveryService.getArticleEngagementStats(article.getId());
        
        return buildArticleDisplayResponse(article, engagementStats, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getArticleStats(String slug) {
        ArticleResponse article = articleService.getArticleBySlug(slug);
        Map<String, Long> engagementStats = discoveryService.getArticleEngagementStats(article.getId());
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("viewCount", article.getViewCount());
        stats.put("formattedViewCount", ArticleUtils.formatViewCount(article.getViewCount()));
        stats.put("readTime", article.getReadTime());
        stats.put("engagement", engagementStats);
        stats.put("engagementRate", calculateEngagementRate(article.getViewCount(), engagementStats));
        stats.put("contentQuality", calculateContentQualityScore(article.getContent()));
        stats.put("readingLevel", assessReadingLevel(article.getContent()));
        
        return stats;
    }

    @Override
    @Transactional
    public Map<String, Object> trackReadingProgress(String slug, String userEmail, int scrollPercentage) {
        ArticleResponse article = articleService.getArticleBySlug(slug);
        
        // Track reading progress
        if (scrollPercentage > 50 && userEmail != null) {
            try {
                discoveryService.trackEngagement(userEmail, article.getId(), "READ");
            } catch (Exception e) {
                // Continue without tracking if there's an error
            }
        }
        
        Map<String, Object> progress = new HashMap<>();
        progress.put("scrollPercentage", scrollPercentage);
        progress.put("estimatedTimeRemaining", calculateTimeRemaining(article.getReadTime(), scrollPercentage));
        progress.put("isCompleted", scrollPercentage >= 90);
        progress.put("progressPercentage", scrollPercentage);
        
        return progress;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getRelatedArticles(String slug, int limit) {
        ArticleResponse article = articleService.getArticleBySlug(slug);
        
        Map<String, Object> related = new HashMap<>();
        related.put("articleId", article.getId());
        related.put("tags", article.getTags());
        related.put("author", article.getAuthorUsername());
        related.put("limit", limit);
        related.put("message", "Related articles would be fetched based on tags and author");
        
        return related;
    }

    @Override
    public double calculateContentQualityScore(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0.0;
        }

        double score = 0.0;
        
        // Word count (0-25 points)
        int wordCount = content.split("\\s+").length;
        score += Math.min(25, wordCount / 10);
        
        // Paragraph count (0-15 points)
        int paragraphCount = content.split("\n\n").length;
        score += Math.min(15, paragraphCount * 2);
        
        // Headers (0-15 points)
        int headerCount = countOccurrences(content, "\n#");
        score += Math.min(15, headerCount * 3);
        
        // Images (0-10 points)
        int imageCount = countOccurrences(content, "![");
        score += Math.min(10, imageCount * 2);
        
        // Code blocks (0-10 points)
        int codeBlockCount = countOccurrences(content, "```") / 2;
        score += Math.min(10, codeBlockCount * 2);
        
        // Links (0-10 points)
        int linkCount = countOccurrences(content, "](http");
        score += Math.min(10, linkCount * 2);
        
        // Lists (0-10 points)
        int listCount = countOccurrences(content, "\n- ") + countOccurrences(content, "\n* ");
        score += Math.min(10, listCount);
        
        // Bold/italic text (0-5 points)
        int formattingCount = countOccurrences(content, "**") + countOccurrences(content, "*");
        score += Math.min(5, formattingCount / 2);
        
        return Math.min(100, score);
    }

    @Override
    public Map<String, String> generateSeoMetadata(ArticleResponse article) {
        Map<String, String> metadata = new HashMap<>();
        
        metadata.put("title", article.getTitle());
        metadata.put("description", article.getSummary() != null ? 
            article.getSummary().substring(0, Math.min(160, article.getSummary().length())) : 
            "Read this article on Medium Clone");
        metadata.put("keywords", String.join(", ", article.getTags()));
        metadata.put("author", article.getAuthorUsername());
        metadata.put("publishedTime", article.getPublishedAt() != null ? 
            article.getPublishedAt().toString() : "");
        metadata.put("modifiedTime", article.getUpdatedAt().toString());
        metadata.put("canonicalUrl", "/articles/" + article.getSlug());
        metadata.put("ogTitle", article.getTitle());
        metadata.put("ogDescription", article.getSummary());
        metadata.put("ogImage", article.getCoverImageUrl());
        metadata.put("ogType", "article");
        metadata.put("twitterCard", "summary_large_image");
        
        return metadata;
    }

    @Override
    public Map<String, Object> analyzeContentStructure(String content) {
        Map<String, Object> structure = new HashMap<>();
        
        if (content == null || content.trim().isEmpty()) {
            return structure;
        }
        
        structure.put("wordCount", content.split("\\s+").length);
        structure.put("paragraphCount", content.split("\n\n").length);
        structure.put("sentenceCount", content.split("[.!?]+").length);
        structure.put("headerCount", countOccurrences(content, "\n#"));
        structure.put("imageCount", countOccurrences(content, "!["));
        structure.put("codeBlockCount", countOccurrences(content, "```") / 2);
        structure.put("linkCount", countOccurrences(content, "](http"));
        structure.put("listCount", countOccurrences(content, "\n- ") + countOccurrences(content, "\n* "));
        structure.put("hasTableOfContents", content.contains("## Table of Contents") || content.contains("## Contents"));
        structure.put("averageWordsPerParagraph", calculateAverageWordsPerParagraph(content));
        structure.put("readingComplexity", assessReadingLevel(content));
        
        return structure;
    }

    @Override
    public String assessReadingLevel(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "Unknown";
        }

        // Simple Flesch Reading Ease calculation
        int sentences = content.split("[.!?]+").length;
        int words = content.split("\\s+").length;
        int syllables = countSyllables(content);
        
        if (sentences == 0 || words == 0) {
            return "Unknown";
        }
        
        double fleschScore = 206.835 - (1.015 * (words / (double) sentences)) - (84.6 * (syllables / (double) words));
        
        if (fleschScore >= 90) return "Very Easy";
        else if (fleschScore >= 80) return "Easy";
        else if (fleschScore >= 70) return "Fairly Easy";
        else if (fleschScore >= 60) return "Standard";
        else if (fleschScore >= 50) return "Fairly Difficult";
        else if (fleschScore >= 30) return "Difficult";
        else return "Very Difficult";
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> calculateEngagementMetrics(Long articleId) {
        Map<String, Long> engagementStats = discoveryService.getArticleEngagementStats(articleId);
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalEngagements", engagementStats.values().stream().mapToLong(Long::longValue).sum());
        metrics.put("engagementBreakdown", engagementStats);
        metrics.put("engagementRate", calculateEngagementRate(0, engagementStats));
        metrics.put("mostPopularEngagement", findMostPopularEngagement(engagementStats));
        
        return metrics;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPerformanceInsights(String slug) {
        ArticleResponse article = articleService.getArticleBySlug(slug);
        Map<String, Long> engagementStats = discoveryService.getArticleEngagementStats(article.getId());
        
        Map<String, Object> insights = new HashMap<>();
        insights.put("viewCount", article.getViewCount());
        insights.put("readTime", article.getReadTime());
        insights.put("engagementRate", calculateEngagementRate(article.getViewCount(), engagementStats));
        insights.put("contentQuality", calculateContentQualityScore(article.getContent()));
        insights.put("readingLevel", assessReadingLevel(article.getContent()));
        insights.put("isPopular", article.getViewCount() > 1000);
        insights.put("isTrending", article.getViewCount() > 5000);
        insights.put("optimizationScore", calculateOptimizationScore(article));
        
        return insights;
    }

    @Override
    public Map<String, String> generateSocialSharingData(ArticleResponse article) {
        Map<String, String> socialData = new HashMap<>();
        
        socialData.put("title", article.getTitle());
        socialData.put("description", article.getSummary());
        socialData.put("url", "/articles/" + article.getSlug());
        socialData.put("image", article.getCoverImageUrl());
        socialData.put("author", article.getAuthorUsername());
        socialData.put("readTime", article.getReadTime() + " min read");
        socialData.put("tags", String.join(", ", article.getTags()));
        
        return socialData;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> validateArticle(String slug) {
        ArticleResponse article = articleService.getArticleBySlug(slug);
        
        Map<String, Object> validation = new HashMap<>();
        validation.put("isValid", true);
        validation.put("suggestions", new HashMap<String, String>());
        
        // Check title length
        if (article.getTitle().length() < 10) {
            validation.put("isValid", false);
            ((Map<String, String>) validation.get("suggestions")).put("title", "Title should be at least 10 characters long");
        }
        
        // Check content length
        if (article.getContent().length() < 100) {
            validation.put("isValid", false);
            ((Map<String, String>) validation.get("suggestions")).put("content", "Content should be at least 100 characters long");
        }
        
        // Check for images
        if (!article.getContent().contains("![") && article.getCoverImageUrl() == null) {
            ((Map<String, String>) validation.get("suggestions")).put("images", "Consider adding images to make your article more engaging");
        }
        
        // Check for headers
        if (!article.getContent().contains("\n#")) {
            ((Map<String, String>) validation.get("suggestions")).put("headers", "Consider adding headers to structure your content");
        }
        
        validation.put("qualityScore", calculateContentQualityScore(article.getContent()));
        validation.put("readingLevel", assessReadingLevel(article.getContent()));
        
        return validation;
    }

    // Helper methods

    private ArticleDisplayResponse buildArticleDisplayResponse(ArticleResponse article, 
                                                             Map<String, Long> engagementStats, 
                                                             String userEmail) {
        return ArticleDisplayResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .summary(article.getSummary())
                .tags(article.getTags())
                .coverImageUrl(article.getCoverImageUrl())
                .slug(article.getSlug())
                .published(article.isPublished())
                .authorUsername(article.getAuthorUsername())
                .authorImage(article.getAuthorImage())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .publishedAt(article.getPublishedAt())
                .readTime(article.getReadTime())
                .viewCount(article.getViewCount())
                .formattedViewCount(ArticleUtils.formatViewCount(article.getViewCount()))
                .readTimeText(formatReadTime(article.getReadTime()))
                .engagementRate(calculateEngagementRate(article.getViewCount(), engagementStats))
                .isPopular(article.getViewCount() > 1000)
                .isTrending(article.getViewCount() > 5000)
                .engagementStats(engagementStats)
                .metaDescription(generateMetaDescription(article))
                .ogImage(article.getCoverImageUrl())
                .canonicalUrl("/articles/" + article.getSlug())
                .hasImages(article.getContent().contains("![") || article.getCoverImageUrl() != null)
                .hasCodeBlocks(article.getContent().contains("```"))
                .hasLinks(article.getContent().contains("](http"))
                .wordCount(article.getContent().split("\\s+").length)
                .paragraphCount(article.getContent().split("\n\n").length)
                .shareCount(engagementStats.getOrDefault("share", 0L).intValue())
                .bookmarkCount(engagementStats.getOrDefault("bookmark", 0L).intValue())
                .commentCount(engagementStats.getOrDefault("comment", 0L).intValue())
                .publicationStatus(article.isPublished() ? "Published" : "Draft")
                .lastModified(article.getUpdatedAt())
                .readingLevel(assessReadingLevel(article.getContent()))
                .language("en")
                .build();
    }

    private String formatReadTime(int minutes) {
        if (minutes < 1) {
            return "Less than 1 min read";
        } else if (minutes == 1) {
            return "1 min read";
        } else {
            return minutes + " min read";
        }
    }

    private double calculateEngagementRate(int viewCount, Map<String, Long> engagementStats) {
        long likes = engagementStats.getOrDefault("like", 0L);
        long comments = engagementStats.getOrDefault("comment", 0L);
        long shares = engagementStats.getOrDefault("share", 0L);
        
        return ArticleUtils.calculateEngagementRate(viewCount, (int) likes, (int) comments, (int) shares);
    }

    private int calculateTimeRemaining(int totalMinutes, int scrollPercentage) {
        int remainingPercentage = 100 - scrollPercentage;
        return (int) Math.ceil((totalMinutes * remainingPercentage) / 100.0);
    }

    private int countOccurrences(String text, String substring) {
        if (text == null || substring == null) {
            return 0;
        }
        
        int count = 0;
        int lastIndex = 0;
        
        while (lastIndex != -1) {
            lastIndex = text.indexOf(substring, lastIndex);
            if (lastIndex != -1) {
                count++;
                lastIndex += substring.length();
            }
        }
        
        return count;
    }

    private double calculateAverageWordsPerParagraph(String content) {
        String[] paragraphs = content.split("\n\n");
        if (paragraphs.length == 0) return 0;
        
        int totalWords = content.split("\\s+").length;
        return (double) totalWords / paragraphs.length;
    }

    private int countSyllables(String text) {
        // Simple syllable counting - count vowel groups
        String lowerText = text.toLowerCase();
        int syllables = 0;
        boolean prevVowel = false;
        
        for (char c : lowerText.toCharArray()) {
            boolean isVowel = "aeiouy".indexOf(c) >= 0;
            if (isVowel && !prevVowel) {
                syllables++;
            }
            prevVowel = isVowel;
        }
        
        return Math.max(1, syllables);
    }

    private String findMostPopularEngagement(Map<String, Long> engagementStats) {
        return engagementStats.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("none");
    }

    private String generateMetaDescription(ArticleResponse article) {
        if (article.getSummary() != null && !article.getSummary().trim().isEmpty()) {
            return article.getSummary().length() > 160 ? 
                article.getSummary().substring(0, 157) + "..." : 
                article.getSummary();
        }
        return "Read " + article.getTitle() + " by " + article.getAuthorUsername();
    }

    private double calculateOptimizationScore(ArticleResponse article) {
        double score = 0.0;
        
        // Title optimization (0-20 points)
        if (article.getTitle().length() >= 30 && article.getTitle().length() <= 60) {
            score += 20;
        } else if (article.getTitle().length() >= 20 && article.getTitle().length() <= 70) {
            score += 15;
        } else {
            score += 10;
        }
        
        // Content length (0-20 points)
        int wordCount = article.getContent().split("\\s+").length;
        if (wordCount >= 300) {
            score += 20;
        } else if (wordCount >= 150) {
            score += 15;
        } else {
            score += 10;
        }
        
        // Images (0-15 points)
        if (article.getCoverImageUrl() != null) {
            score += 15;
        } else if (article.getContent().contains("![") && article.getContent().contains("](")) {
            score += 10;
        }
        
        // Tags (0-15 points)
        if (article.getTags().size() >= 3) {
            score += 15;
        } else if (article.getTags().size() >= 1) {
            score += 10;
        }
        
        // Summary (0-15 points)
        if (article.getSummary() != null && article.getSummary().length() >= 50) {
            score += 15;
        } else if (article.getSummary() != null && article.getSummary().length() >= 20) {
            score += 10;
        }
        
        // Read time (0-15 points)
        if (article.getReadTime() >= 3 && article.getReadTime() <= 15) {
            score += 15;
        } else if (article.getReadTime() >= 1 && article.getReadTime() <= 20) {
            score += 10;
        }
        
        return Math.min(100, score);
    }
} 