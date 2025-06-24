package com.medium_clone.user.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String username;
    private String bio;
    private String image; // URL for now
} 