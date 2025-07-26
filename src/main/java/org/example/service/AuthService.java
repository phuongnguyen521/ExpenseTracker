package org.example.service;

import org.example.dto.AppUserDTO;
import org.example.dto.AuthDTO;
import org.example.dto.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO registerUser(AppUserDTO appUserDTO);
    AuthResponseDTO loginUser(AuthDTO authDTO);

}
