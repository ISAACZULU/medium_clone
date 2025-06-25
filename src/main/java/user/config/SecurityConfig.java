package user.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors().and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/register", "/api/users/login").permitAll()
                        .requestMatchers("/api/users/password-reset/**").permitAll()
                        .requestMatchers("/api/profiles/**").permitAll()
                        .requestMatchers("/api/articles").permitAll()
                        .requestMatchers("/api/articles/{slug}").permitAll()
                        .requestMatchers("/api/articles/tag/**").permitAll()
                        .requestMatchers("/api/articles/search").permitAll()
                        .requestMatchers("/api/articles/author/**").permitAll()
                        .requestMatchers("/api/discovery/trending").permitAll()
                        .requestMatchers("/api/discovery/search").permitAll()
                        .requestMatchers("/api/discovery/search/tags").permitAll()
                        .requestMatchers("/api/discovery/recent").permitAll()
                        .requestMatchers("/api/discovery/articles/{articleId}/engagement").permitAll()
                        .requestMatchers("/api/discovery/search/quick").permitAll()
                        .requestMatchers("/api/discovery/author/**").permitAll()
                        .requestMatchers("/api/discovery/date-range").permitAll()
                        .requestMatchers("/api/display/articles/{slug}").permitAll()
                        .requestMatchers("/api/display/articles/{slug}/preview").permitAll()
                        .requestMatchers("/api/display/articles/{slug}/stats").permitAll()
                        .requestMatchers("/api/display/articles/{slug}/related").permitAll()
                        .requestMatchers("/api/display/validate-slug/**").permitAll()
                        .requestMatchers("/api/display/calculate-read-time").permitAll()
                        .requestMatchers("/api/**").authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(jwtUtil::loadUserByUsername)
                .passwordEncoder(passwordEncoder())
                .and()
                .build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil);
    }
}
