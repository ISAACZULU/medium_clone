package user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medium_clone.user.dto.UpdateProfileRequest;
import com.medium_clone.user.dto.UserProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import user.service.UserService;
import user.config.JwtUtil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    public void testUpdateProfile() throws Exception {
        String token = "test.jwt.token";
        String email = "test@example.com";
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("newuser");
        request.setBio("New bio");
        request.setImage("http://image.url");

        UserProfileResponse response = new UserProfileResponse();
        response.setEmail(email);
        response.setUsername("newuser");
        response.setBio("New bio");
        response.setImage("http://image.url");

        when(jwtUtil.extractEmail(token)).thenReturn(email);
        when(userService.updateUserProfile(eq(email), any(UpdateProfileRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/profile")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.bio").value("New bio"))
                .andExpect(jsonPath("$.image").value("http://image.url"));
    }
} 