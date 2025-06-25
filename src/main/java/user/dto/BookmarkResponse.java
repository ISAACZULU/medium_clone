package com.medium_clone.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookmarkResponse {
    private Long id;
    private Long articleId;
    private Long collectionId;
    private LocalDateTime createdAt;
} 