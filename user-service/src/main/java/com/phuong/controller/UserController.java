package com.phuong.controller;

import com.phuong.exception.ResourceNotFoundException;
import com.phuong.model.AppUser;
import com.phuong.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @GetMapping("/username/{username}")
    public ResponseEntity<AppUser> getUserByUsername(@PathVariable String username) {
        log.info("Received request to get user by username: {}", username);
        try {
            AppUser user = userService.findByUsername(username);
            log.info("Found user with username: {}", username);
            return ResponseEntity.ok(user);

        } catch (ResourceNotFoundException ex) {
            log.warn("User not found with username: {}", username);
            throw ex; // Let the global expection handler manage it
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppUser> getUserById(@PathVariable Long id) {
        log.info("Received request to get user by Id: {}", id);
        Optional<AppUser> user = userService.findUserById(id);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        log.info("Found user with Id: {}", id);
        return ResponseEntity.ok(user.get());
    }
}
