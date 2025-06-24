package user.service;

import com.medium_clone.user.dto.RegisterRequest;
import com.medium_clone.user.dto.RegisterResponse;
import com.medium_clone.user.entity.User;
import user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

        // Generate JWT token (in real application, this would be handled by authentication service)
        String token = UUID.randomUUID().toString();

        // Create response
        return RegisterResponse.success(
                "User registered successfully.",
                token,
                savedUser.getId().toString(),
                savedUser.getUsername()
        );
    }
}
