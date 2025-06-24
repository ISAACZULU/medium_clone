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
public class ArticleVersionResponse {
    
    private Long id;
    private Integer versionNumber;
    private String title;
    private String content;
    private String summary;
    private Set<String> tags;
    private String coverImageUrl;
    private String slug;
    private String changeDescription;
    private String editorEmail;
    private LocalDateTime createdAt;
} 