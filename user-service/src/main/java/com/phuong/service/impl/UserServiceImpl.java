package com.phuong.service.impl;

import com.phuong.model.AppUser;
import com.phuong.repository.UserRepository;
import com.phuong.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public AppUser saveUser(AppUser user) {
        return userRepository.save(user);
    }

    @Override
    public AppUser findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElse(null);
    }

    @Override
    public Optional<AppUser> findUserById(Long id) {
        return userRepository.findById(id);
    }
}
