package com.medium_clone.user.dto;

import lombok.Data;

@Data
public class CommentRequest {
    private String content;
    private Long parentId; // null for root comment, set for reply
} 