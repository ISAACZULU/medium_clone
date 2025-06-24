@Bean
public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**")
                    .allowedOrigins("http://localhost:8081", // React Native Metro bundler
                            "http://10.0.2.2:8081", // Android emulator
                            "http://localhost:3000", // iOS simulator
                            "http://127.0.0.1:3000") // iOS simulator alternative
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true);
        }
    };
}