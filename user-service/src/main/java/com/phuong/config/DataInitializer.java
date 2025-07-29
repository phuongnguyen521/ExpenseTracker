package com.phuong.config;

import com.phuong.model.AppUser;
import com.phuong.model.Role;
import com.phuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findAll().isEmpty()){

            // Admin
            AppUser admin = new AppUser();
            admin.setFullName("admin");
            admin.setRole(Role.ADMIN);
            admin.setUsername("admin");
            admin.setPassword("admin123");

            // User
            AppUser user = new AppUser();
            admin.setFullName("phuong");
            admin.setRole(Role.USER);
            admin.setUsername("phuong");
            admin.setPassword("phuong123");

            userRepository.save(admin);

            userRepository.save(user);

            log.info("Admin: {} - {}", admin.getUsername(), admin.getPassword());
            log.info("User: {} - {}", user.getUsername(), user.getPassword());
        }
    }
}
