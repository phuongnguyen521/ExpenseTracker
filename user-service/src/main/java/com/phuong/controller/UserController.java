package com.phuong.controller;

import com.phuong.model.AppUser;
import com.phuong.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/user/username/{username}")
    public ResponseEntity<AppUser> getUserByUsername(@PathVariable String username) {
        AppUser user = userService.findByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<Optional<AppUser>> getUserById(@PathVariable Long id) {
        Optional<AppUser> user = userService.findUserById(id);
        return ResponseEntity.ok(user);
    }
}
