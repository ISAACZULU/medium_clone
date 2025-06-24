package com.medium_clone.user.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleResponse {
    
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
} 