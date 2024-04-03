package com.resendil.loginauthapi.controllers;

import com.resendil.loginauthapi.DTOS.LoginRequestDTO;
import com.resendil.loginauthapi.DTOS.RegisterRequestDTO;
import com.resendil.loginauthapi.DTOS.ResponseDTO;
import com.resendil.loginauthapi.domain.user.User;
import com.resendil.loginauthapi.infra.security.TokenService;
import com.resendil.loginauthapi.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginRequestDTO body) {
        User user = this.userRepository.findByEmail(body.email()).orElseThrow(() -> new RuntimeException("User not found"));
        if (passwordEncoder.matches(body.password(), user.getPassword())) {
            String token = this.tokenService.generateToken(user);
            return ResponseEntity.ok(new ResponseDTO(user.getName(), token));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterRequestDTO body) {

        Optional<User> user = userRepository.findByEmail(body.email());

        if (user.isEmpty()) {
            User newUser = new User();
            newUser.setPassword(passwordEncoder.encode(body.password()));
            newUser.setEmail(body.email());
            newUser.setName(body.name());
            userRepository.save(newUser);

            String token = tokenService.generateToken(newUser);
            return ResponseEntity.ok(new ResponseDTO(newUser.getName(), token));

        } else if (user.isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.badRequest().build();
    }

}
