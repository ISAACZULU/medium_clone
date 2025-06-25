package com.medium_clone.user.dto;

import lombok.Data;

@Data
public class BookmarkCollectionRequest {
    private String name;
    private String description;
    private boolean isPublic;
} 