package com.darikh.dms.controller;

import com.darikh.dms.model.User;
import com.darikh.dms.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        user.setRole("USER");
        return userRepository.save(user);
    }

    @PostMapping("/login")
    public User login(@RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Passwort falsch");
        }

        return user;
    }
}