package com.medium_clone.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String message;
    private boolean success;
    private String token;
    private String userId;
    private String username;

    public static LoginResponse success(String message, String token, String userId, String username) {
        return new LoginResponse(message, true, token, userId, username);
    }

    public static LoginResponse failure(String message) {
        return new LoginResponse(message, false, null, null, null);
    }
}
