package com.fpt.authservice.controller;

import com.fpt.authservice.dto.UserResponse;
import com.fpt.authservice.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Chỉ ADMIN (cấu hình trong SecurityConfig: /api/v1/users/** hasRole ADMIN). */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public List<UserResponse> getAll() {
        return userService.findAll();
    }
}
