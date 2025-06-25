package com.medium_clone.user.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ArticleSearchRequest {
    
    private String keywords; // Search in title and content
    
    private Set<String> tags; // Filter by tags
    
    private String authorUsername; // Filter by author
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fromDate; // Articles from this date
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime toDate; // Articles until this date
    
    private String sortBy = "publishedAt"; // publishedAt, viewCount, readTime, title
    
    private String sortOrder = "desc"; // asc, desc
    
    private int page = 0;
    
    private int size = 10;
    
    private boolean publishedOnly = true; // Only published articles
} 