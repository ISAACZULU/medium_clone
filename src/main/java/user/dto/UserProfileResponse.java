package com.medium_clone.user.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserProfileResponse {
    private Long id;
    private String email;
    private String username;
    private String bio;
    private String image;
    private LocalDateTime createdAt;

    public UserProfileResponse() {}

    public UserProfileResponse(String email, String username, String bio, String image) {
        this.email = email;
        this.username = username;
        this.bio = bio;
        this.image = image;
    }
} 