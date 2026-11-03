package com.fpt.authservice.dto;

import jakarta.validation.constraints.NotBlank;

/** Body của POST /api/v1/auth/login. identifier là username hoặc email. */
public record LoginRequest(
        @NotBlank(message = "Username or email must not be blank")
        String identifier,

        @NotBlank(message = "Password must not be blank")
        String password) {
}
