package org.example.service.impl;

import org.example.model.AppUser;
import org.example.repository.UserRepository;
import org.example.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AppUser saveUser(AppUser user) {
        return userRepository.save(user);
    }

    @Override
    public AppUser findByUsername(String username) {
        return userRepository
                .findAll().stream()
                .filter(user ->
                        user.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Optional<AppUser> findUserById(Long id) {
        return userRepository.findById(id);
    }
}
