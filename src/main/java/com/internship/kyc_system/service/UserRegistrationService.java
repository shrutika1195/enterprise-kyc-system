package com.internship.kyc_system.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserRegistrationService implements UserDetailsService {

    // Thread-safe map to store users dynamically
    private final Map<String, UserDetails> users = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;

        // Pre-load the Admin Account (Username: shrutika, Password: shrutika1195)
        users.put("shrutika", User.builder()
                .username("shrutika")
                .password(passwordEncoder.encode("shrutika1195"))
                .roles("ADMIN")
                .build());

        // Pre-load a Demo Citizen Account (Username: user, Password: user123)
        users.put("user", User.builder()
                .username("user")
                .password(passwordEncoder.encode("user123"))
                .roles("USER")
                .build());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails user = users.get(username.toLowerCase().trim());
        if (user == null) {
            throw new UsernameNotFoundException("No account found for: " + username);
        }
        return user;
    }

    public void registerUser(String username, String rawPassword, String firstName, String lastName) {
        String key = username.toLowerCase().trim();

        if (users.containsKey(key)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");
        }

        UserDetails newUser = User.builder()
                .username(key)
                .password(passwordEncoder.encode(rawPassword))
                .roles("USER")
                .build();

        users.put(key, newUser);

        System.out.println("=========================================");
        System.out.println("[AUTH] New User Registered: " + key);
        System.out.println("=========================================");
    }

    public boolean userExists(String username) {
        return users.containsKey(username.toLowerCase().trim());
    }
}