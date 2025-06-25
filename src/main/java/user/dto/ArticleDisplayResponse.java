package com.medium_clone.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDisplayResponse {
    
    private Long id;
    private String title;
    private String content;
    private String summary;
    private Set<String> tags;
    private String coverImageUrl;
    private String slug;
    private boolean published;
    private String authorUsername;
    private String authorImage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
    private Integer readTime;
    private Integer viewCount;
    
    // Enhanced display information
    private String formattedViewCount;
    private String readTimeText;
    private double engagementRate;
    private boolean isPopular;
    private boolean isTrending;
    
    // Engagement statistics
    private Map<String, Long> engagementStats;
    
    // Reading progress (for authenticated users)
    private Integer scrollPercentage;
    private Integer estimatedTimeRemaining;
    private boolean isCompleted;
    
    // Related articles info
    private Set<String> relatedTags;
    private String relatedAuthor;
    
    // SEO and sharing information
    private String metaDescription;
    private String ogImage;
    private String canonicalUrl;
    
    // Content quality indicators
    private boolean hasImages;
    private boolean hasCodeBlocks;
    private boolean hasLinks;
    private int wordCount;
    private int paragraphCount;
    
    // Social sharing metrics
    private int shareCount;
    private int bookmarkCount;
    private int commentCount;
    
    // Author information
    private String authorBio;
    private int authorArticleCount;
    private int authorFollowerCount;
    
    // Publication information
    private String publicationStatus;
    private LocalDateTime lastModified;
    private String lastModifiedBy;
    
    // Accessibility information
    private String readingLevel;
    private boolean hasTableOfContents;
    private String language;
    
    // Performance metrics
    private double loadTime;
    private boolean isOptimized;
    private String optimizationScore;
} 