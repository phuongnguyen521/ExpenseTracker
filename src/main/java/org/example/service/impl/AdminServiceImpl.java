package org.example.service.impl;

import org.example.model.AppUser;
import org.example.repository.UserRepository;
import org.example.service.AdminService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;

    public AdminServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<AppUser> getAllUsers() {
        return userRepository.findAll();
    }
}
