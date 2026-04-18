package com.example.jwtDemo.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.jwtDemo.entity.User;
import com.example.jwtDemo.repository.UserRepository;

@Configuration
public class AdminSeeder {

    @Bean
    public CommandLineRunner seedAdmin(UserRepository userRepository,
                                       PasswordEncoder passwordEncoder) {
        return args -> {
            String adminUsername = "admin";

            if (!userRepository.existsByUsername(adminUsername)) {
                User admin = new User(
                        "Admin",
                        adminUsername,
                        passwordEncoder.encode("admin123"),
                        "ADMIN"
                );
                userRepository.save(admin);
                System.out.println("Admin user created: admin / admin123");
            }
        };
    }
}