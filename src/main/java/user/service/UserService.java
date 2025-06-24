package user.service;

import com.medium_clone.user.dto.RegisterRequest;
import com.medium_clone.user.dto.RegisterResponse;
import com.medium_clone.user.dto.LoginRequest;
import com.medium_clone.user.dto.LoginResponse;
import com.medium_clone.user.entity.User;
import com.medium_clone.user.repository.UserRepository;
import com.medium_clone.user.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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
        String token = jwtUtil.generateToken(user.getEmail());

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
}