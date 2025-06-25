package com.medium_clone.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private Long articleId;
    private Long userId;
    private String username;
    private String userImage;
    private Long parentId;
    private String content;
    private boolean deleted;
    private boolean flagged;
    private String flagReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentResponse> replies;
} 