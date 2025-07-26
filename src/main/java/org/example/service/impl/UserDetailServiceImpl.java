package org.example.service.impl;

import org.example.model.AppUser;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

@Service
public class UserDetailServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<AppUser> appUser = userRepository.findAll()
                .stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .findFirst();
        if (appUser.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }

        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("ROLE_" + appUser.get().getRole().name());

        return new User(appUser.get().getUsername(),
                appUser.get().getPassword(),
                Collections.singleton(authority));
    }
}
