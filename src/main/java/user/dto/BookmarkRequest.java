package com.medium_clone.user.dto;

import lombok.Data;

@Data
public class BookmarkRequest {
    private Long articleId;
    private Long collectionId; // null for default
} 