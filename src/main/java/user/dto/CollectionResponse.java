package com.medium_clone.user.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class CollectionResponse {
    private Long id;
    private String name;
    private String description;
    private boolean isPublic;
    private Long ownerId;
    private String ownerUsername;
    private Set<Long> collaboratorIds;
    private Set<String> collaboratorUsernames;
    private Set<Long> articleIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 