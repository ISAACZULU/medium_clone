package com.medium_clone.user.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Data
public class UpdateArticleRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    @Size(max = 500, message = "Summary cannot exceed 500 characters")
    private String summary;
    
    private Set<String> tags;
    
    private String coverImageUrl;
    
    private boolean published = false;
    
    @Size(max = 200, message = "Change description cannot exceed 200 characters")
    private String changeDescription; // Optional description of what changed
} 