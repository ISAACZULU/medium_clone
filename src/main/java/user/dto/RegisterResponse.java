package com.medium_clone.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    
    private String message;
    private boolean success;
    private String token;
    private String userId;
    private String username;

    public RegisterResponse(String message, boolean success, String token, String userId, String username) {
        this.message = message;
        this.success = success;
        this.token = token;
        this.userId = userId;
        this.username = username;
    }

    public static RegisterResponse success(String message, String token, String userId, String username) {
        return new RegisterResponse(message, true, token, userId, username);
    }
    
    public static RegisterResponse failure(String message) {
        return new RegisterResponse(message, false, null, null, null);
    }
}