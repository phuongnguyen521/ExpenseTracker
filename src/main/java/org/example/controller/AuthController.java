package org.example.controller;

import org.example.dto.AppUserDTO;
import org.example.dto.AuthDTO;
import org.example.dto.AuthResponseDTO;
import org.example.model.AppUser;
import org.example.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponseDTO> signup(@RequestBody AppUserDTO appUserDTO) {
        AuthResponseDTO response = authService.registerUser(appUserDTO);
        showMessage(response.getToken(), response.getMessage());

        if ("Success".equalsIgnoreCase(response.getMessage())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthDTO authDTO) {
        AuthResponseDTO response = authService.loginUser(authDTO);
        showMessage(response.getToken(), response.getMessage());

        if ("Success".equalsIgnoreCase(response.getMessage())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    private void showMessage(String token, String message){
        System.out.println("Token: " + token + "\nMessage: " + message);
    }
}
