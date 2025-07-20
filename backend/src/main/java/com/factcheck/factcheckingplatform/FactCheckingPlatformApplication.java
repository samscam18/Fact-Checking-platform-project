package com.factcheck.factcheckingplatform;

import com.factcheck.factcheckingplatform.model.Claim;
import com.factcheck.factcheckingplatform.model.User;
import com.factcheck.factcheckingplatform.repository.ClaimRepository;
import com.factcheck.factcheckingplatform.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;

@SpringBootApplication
@EnableMethodSecurity // Enable method-level security for @PreAuthorize
public class FactCheckingPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(FactCheckingPlatformApplication.class, args);
    }

    // Configure CORS globally for development purposes
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:8000", "http://127.0.0.1:8000", "http://localhost:5500", "http://127.0.0.1:5500") // Add your frontend server URL
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    // CommandLineRunner to populate initial data and users
    @Bean
    public CommandLineRunner demoData(UserRepository userRepository, ClaimRepository claimRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Create users if they don't exist
            if (userRepository.findByUsername("user").isEmpty()) {
                User user = new User("user", passwordEncoder.encode("password"), new HashSet<>(Arrays.asList("ROLE_USER")));
                userRepository.save(user);
            }
            if (userRepository.findByUsername("factchecker").isEmpty()) {
                User factChecker = new User("factchecker", passwordEncoder.encode("factcheckpass"), new HashSet<>(Arrays.asList("ROLE_USER", "ROLE_FACT_CHECKER")));
                userRepository.save(factChecker);
            }

            // Create claims if they don't exist
            if (claimRepository.count() == 0) {
                claimRepository.save(new Claim("user", "Drinking hot water cures COVID-19.", "https://example.com/fake-news-1"));
                claimRepository.save(new Claim("user", "Bengaluru Metro Phase 3 will be completed by 2026.", "https://bmtc.gov.in/news/metro-phase-3-update"));
                claimRepository.save(new Claim("user", "New tax introduced on online transactions in Karnataka.", "https://twitter.com/some_news/status/12345"));

                // Simulate some verified claims
                Claim claim1 = new Claim("user", "AI will replace all software developers by 2030.", "https://techcrunch.com/article/ai-job-impact");
                claim1.setStatus("Fact-Checked");
            }
        };
    }
}
