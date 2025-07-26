package org.example.service.impl;

import org.example.dto.AppUserDTO;
import org.example.dto.AuthDTO;
import org.example.dto.AuthResponseDTO;
import org.example.model.AppUser;
import org.example.model.Role;
import org.example.service.AuthService;
import org.example.service.UserService;
import org.example.utils.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserService userService,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public AuthResponseDTO registerUser(AppUserDTO appUserDTO) {
        // Check if username already exists
        if (userService.findByUsername(appUserDTO.getUsername()) != null) {
            return new AuthResponseDTO(null, "error: Username is already taken.");
        }

        // Create new user
        AppUser appUser = new AppUser();
        appUser.setFullName(appUserDTO.getFullName());
        appUser.setUsername(appUserDTO.getUsername());
        appUser.setPassword(passwordEncoder.encode(appUserDTO.getPassword()));
        appUser.setRole(Role.USER);

        // Save user to the database
        userService.saveUser(appUser);

        // Automatically log in the user
        AuthDTO authDTO = new AuthDTO();
        authDTO.setUsername(appUserDTO.getUsername());
        authDTO.setPassword(appUserDTO.getPassword());

        return loginUser(authDTO);
    }

    @Override
    public AuthResponseDTO loginUser(AuthDTO authDTO) {
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authDTO.getUsername(),
                            authDTO.getPassword()
                    )
            );

            // Generate JWT token
            final String token = jwtUtil.generateToken(authDTO.getUsername());

            // Return success response
            return new AuthResponseDTO(token, "success");

        } catch (BadCredentialsException e) {
            // Return error response
            return new AuthResponseDTO(null, "error: Invalid username or password.");
        }
    }
}
