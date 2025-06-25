package com.medium_clone.user.dto;

import lombok.Data;
import java.util.Set;

@Data
public class CollectionRequest {
    private String name;
    private String description;
    private boolean isPublic;
    private Set<Long> collaboratorIds; // user IDs
} 