package com.fpt.ecommerce.controller;

import com.fpt.ecommerce.dto.AccountInfo;
import com.fpt.ecommerce.dto.LoginRequest;
import com.fpt.ecommerce.dto.LoginResponse;
import com.fpt.ecommerce.dto.RegisterRequest;
import com.fpt.ecommerce.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register") // Nối biểu mẫu /register với API tạo tài khoản USER thật.
    @ResponseStatus(HttpStatus.CREATED)
    public AccountInfo register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @GetMapping("/me")
    public AccountInfo me(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return authService.currentAccount(authorization);
    }
}