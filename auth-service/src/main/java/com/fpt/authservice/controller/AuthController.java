package com.fpt.authservice.controller;

import com.fpt.authservice.dto.LoginRequest;
import com.fpt.authservice.dto.LoginResponse;
import com.fpt.authservice.dto.RegisterRequest;
import com.fpt.authservice.dto.UserResponse;
import com.fpt.authservice.security.AuthenticatedUser;
import com.fpt.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** Thông tin tài khoản hiện tại, lấy id từ JWT rồi đọc database. */
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public UserResponse me(Authentication authentication) {
        AuthenticatedUser principal = (AuthenticatedUser) authentication.getPrincipal();
        return authService.me(principal.id());
    }
}
