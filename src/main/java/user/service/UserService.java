package user.service;

import com.medium_clone.user.dto.RegisterRequest;
import com.medium_clone.user.dto.RegisterResponse;
import com.medium_clone.user.dto.LoginRequest;
import com.medium_clone.user.dto.LoginResponse;
import com.medium_clone.user.dto.UpdateProfileRequest;
import com.medium_clone.user.dto.UserProfileResponse;
import user.dto.NotificationPreferencesRequest;

import java.util.List;

public interface UserService {
    RegisterResponse registerUser(RegisterRequest request);
    LoginResponse loginUser(LoginRequest request);
    UserProfileResponse updateUserProfile(String email, UpdateProfileRequest request);
    String requestPasswordReset(String email);
    void confirmPasswordReset(String token, String newPassword);
    UserProfileResponse getPublicProfile(String username);
    void followUser(String followerEmail, String usernameToFollow);
    void unfollowUser(String followerEmail, String usernameToUnfollow);
    List<String> getFollowers(String username);
    List<String> getFollowing(String username);
    User getUserByEmail(String email);
    User updateNotificationPreferences(String email, NotificationPreferencesRequest prefs);
}