package user.controller;

import com.medium_clone.user.dto.LoginRequest;
import com.medium_clone.user.dto.LoginResponse;
import com.medium_clone.user.dto.RegisterRequest;
import com.medium_clone.user.dto.RegisterResponse;
import com.medium_clone.user.dto.UpdateProfileRequest;
import com.medium_clone.user.dto.UserProfileResponse;
import com.medium_clone.user.dto.PasswordResetRequest;
import com.medium_clone.user.dto.PasswordResetConfirmRequest;
import com.medium_clone.user.service.UserService;
import user.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/users/" + response.getUserId())
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.loginUser(request);
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // Invalidate the token on the client side by deleting it from storage (stateless JWT)
        return ResponseEntity.ok("Logout successful.");
    }

    @PutMapping("/profile")
    public UserProfileResponse updateProfile(@RequestBody UpdateProfileRequest request,
                                           @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return userService.updateUserProfile(email, request);
    }

    @PostMapping("/password-reset/request")
    public String requestPasswordReset(@RequestBody PasswordResetRequest request) {
        return userService.requestPasswordReset(request.getEmail());
    }

    @PostMapping("/password-reset/confirm")
    public String confirmPasswordReset(@RequestBody PasswordResetConfirmRequest request) {
        userService.confirmPasswordReset(request.getToken(), request.getNewPassword());
        return "Password reset successful";
    }

    @GetMapping("/profiles/{username}")
    public UserProfileResponse getPublicProfile(@PathVariable String username) {
        return userService.getPublicProfile(username);
    }

    @PostMapping("/follow/{username}")
    public void follow(@PathVariable String username,
                       @RequestHeader("Authorization") String authHeader) {
        String email = jwtUtil.extractEmail(authHeader.replace("Bearer ", ""));
        userService.followUser(email, username);
    }

    @DeleteMapping("/unfollow/{username}")
    public void unfollow(@PathVariable String username,
                         @RequestHeader("Authorization") String authHeader) {
        String email = jwtUtil.extractEmail(authHeader.replace("Bearer ", ""));
        userService.unfollowUser(email, username);
    }

    @GetMapping("/followers/{username}")
    public List<String> getFollowers(@PathVariable String username) {
        return userService.getFollowers(username);
    }

    @GetMapping("/following/{username}")
    public List<String> getFollowing(@PathVariable String username) {
        return userService.getFollowing(username);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<String> handleException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred: " + ex.getMessage());
    }
}
