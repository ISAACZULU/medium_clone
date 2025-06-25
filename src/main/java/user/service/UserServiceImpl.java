package user.service;

import com.medium_clone.user.dto.RegisterRequest;
import com.medium_clone.user.dto.RegisterResponse;
import com.medium_clone.user.dto.LoginRequest;
import com.medium_clone.user.dto.LoginResponse;
import com.medium_clone.user.dto.UpdateProfileRequest;
import com.medium_clone.user.dto.UserProfileResponse;
import com.medium_clone.user.entity.User;
import user.repository.UserRepository;
import user.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import user.service.NotificationService;
import user.dto.NotificationPreferencesRequest;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final NotificationService notificationService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public RegisterResponse registerUser(RegisterRequest request) {
        // Validate request
        if (!request.isValid()) {
            throw new IllegalArgumentException("Invalid registration request");
        }

        // Check email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use.");
        }

        // Check username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken.");
        }

        // Create user
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        // Save user
        User savedUser = userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getEmail());

        // Create response
        return RegisterResponse.success(
                "User registered successfully.",
                token,
                savedUser.getId().toString(),
                savedUser.getUsername()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse loginUser(LoginRequest request) {
        try {
            // Validate request
            if (!request.isValid()) {
                return LoginResponse.failure("Invalid login request");
            }

            // Find user by email
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

            // Check password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return LoginResponse.failure("Invalid email or password.");
            }

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail());

            // Create response
            return LoginResponse.success(
                    "Login successful",
                    token,
                    user.getId().toString(),
                    user.getUsername()
            );
        } catch (IllegalArgumentException e) {
            return LoginResponse.failure(e.getMessage());
        } catch (Exception e) {
            return LoginResponse.failure("An error occurred during login");
        }
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setUsername(request.getUsername());
        user.setBio(request.getBio());
        user.setImage(request.getImage());

        User updated = userRepository.save(user);

        return new UserProfileResponse(
            updated.getEmail(),
            updated.getUsername(),
            updated.getBio(),
            updated.getImage()
        );
    }

    @Override
    public String requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        // Generate a JWT token for password reset with a short expiry and a claim indicating reset
        String token = jwtUtil.generatePasswordResetToken(user.getEmail());
        // In a real app, you would send this token via email
        return token;
    }

    @Override
    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        String email = jwtUtil.extractEmail(token);
        if (!jwtUtil.isPasswordResetToken(token)) {
            throw new IllegalArgumentException("Invalid password reset token");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getPublicProfile(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new UserProfileResponse(
            user.getEmail(), // Remove if you don't want to expose email
            user.getUsername(),
            user.getBio(),
            user.getImage()
        );
    }

    @Override
    @Transactional
    public void followUser(String followerEmail, String usernameToFollow) {
        User follower = userRepository.findByEmail(followerEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        User toFollow = userRepository.findByUsername(usernameToFollow)
            .orElseThrow(() -> new IllegalArgumentException("User to follow not found"));

        if (follower.equals(toFollow)) throw new IllegalArgumentException("You can't follow yourself.");

        follower.getFollowing().add(toFollow);
        userRepository.save(follower);
        notificationService.notifyFollow(toFollow, follower);
    }

    @Override
    @Transactional
    public void unfollowUser(String followerEmail, String usernameToUnfollow) {
        User follower = userRepository.findByEmail(followerEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        User toUnfollow = userRepository.findByUsername(usernameToUnfollow)
            .orElseThrow(() -> new IllegalArgumentException("User to unfollow not found"));

        follower.getFollowing().remove(toUnfollow);
        userRepository.save(follower);
    }

    @Override
    public List<String> getFollowers(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getFollowers().stream().map(User::getUsername).toList();
    }

    @Override
    public List<String> getFollowing(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getFollowing().stream().map(User::getUsername).toList();
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @Override
    @Transactional
    public User updateNotificationPreferences(String email, NotificationPreferencesRequest prefs) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setReceiveFollowNotifications(prefs.isReceiveFollowNotifications());
        user.setReceiveClapNotifications(prefs.isReceiveClapNotifications());
        user.setReceiveCommentNotifications(prefs.isReceiveCommentNotifications());
        user.setReceiveMentionNotifications(prefs.isReceiveMentionNotifications());
        user.setReceiveRecommendationNotifications(prefs.isReceiveRecommendationNotifications());
        user.setEmailNotificationsEnabled(prefs.isEmailNotificationsEnabled());
        user.setPushNotificationsEnabled(prefs.isPushNotificationsEnabled());
        user.setEmailDigestFrequency(User.EmailDigestFrequency.valueOf(prefs.getEmailDigestFrequency()));
        return userRepository.save(user);
    }
}
