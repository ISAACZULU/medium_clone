package com.medium_clone.user.dto;

import lombok.Data;

@Data
public class PersonalizedFeedRequest {
    
    private int page = 0;
    
    private int size = 10;
    
    private boolean includeFollowedAuthors = true; // Articles from followed authors
    
    private boolean includeFollowedTags = true; // Articles with followed tags
    
    private boolean includeTrending = true; // Include trending articles
    
    private String sortBy = "publishedAt"; // publishedAt, trending, relevance
    
    private String sortOrder = "desc"; // asc, desc
} 