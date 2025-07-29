package com.phuong.service.impl;

import com.phuong.exception.BusinessRuleException;
import com.phuong.exception.ResourceNotFoundException;
import com.phuong.model.AppUser;
import com.phuong.repository.UserRepository;
import com.phuong.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AppUser saveUser(AppUser user) {
        log.debug("Attempting to save user with username: {}", user.getUsername());

        validateUser(user);

        try {
            AppUser savedUser = userRepository.save(user);
            log.info("Successfully saved user with ID: {} and username: {}",
                    savedUser.getId(), savedUser.getUsername());
            return savedUser;

        } catch (DataIntegrityViolationException ex) {
            log.error("Data integrity violation while saving user with username {}",
                    user.getUsername(), ex);
            throw new BusinessRuleException("Username already existed: " + user.getUsername());
        } catch (DataAccessException ex) {
            log.error("Database error while saving user with username: {}",
                    user.getUsername(), ex);
            throw new RuntimeException("Failed to save user: ", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AppUser findByUsername(String username) {
        log.debug("Searching for user with username: {}", username);
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessRuleException("Username cannot be null or empty");
        }
        try {
            Optional<AppUser> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                log.info("Found user with username: {}", username);
                return user.get();
            } else {
                log.warn("User not found with username: {}", username);
                throw new ResourceNotFoundException("User", "username", username);
            }
        } catch (DataAccessException ex) {
            log.error("Database error while finding user with username: {}",
                    username, ex);
            throw new RuntimeException("Failed to find user: ", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppUser> findUserById(Long id) {
        log.debug("Searching for user by id: {}", id);

        if (id == null || id <= 0) {
            throw new BusinessRuleException("User Id must be a positive number");
        }

        try {
            Optional<AppUser> user = userRepository.findById(id);
            if (user.isPresent()) {
                log.info("Found user with id: {}", id);
            } else {
                log.warn("User not found with id: {}", id);
            }
            return user;
        } catch (DataAccessException ex) {
            log.error("Database error while finding user with id: {}",
                    id, ex);
            throw new RuntimeException("Failed to find user: ", ex);
        }
    }

    private void validateUser(AppUser user) {
        if (user == null) {
            throw new BusinessRuleException("User cannot be null");
        }

        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new BusinessRuleException("Username is required");
        }

        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new BusinessRuleException("Password is required");
        }

        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new BusinessRuleException("Full name is required");
        }

        if (user.getRole() == null) {
            throw new BusinessRuleException("User role is required");
        }
    }
}
