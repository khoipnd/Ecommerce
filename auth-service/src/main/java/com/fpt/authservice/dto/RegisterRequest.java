package com.fpt.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Body của POST /api/v1/auth/register.
 * Không có trường role: quyền luôn là USER, client không được tự chọn ADMIN.
 * Mật khẩu tối đa 72 ký tự vì BCrypt chỉ dùng 72 byte đầu (AuthService kiểm tra thêm theo byte).
 */
public record RegisterRequest(
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email is not valid")
        @Size(max = 100, message = "Email must be at most 100 characters")
        String email,

        @NotBlank(message = "Password must not be blank")
        @Size(min = 6, max = 72, message = "Password must be between 6 and 72 characters")
        String password,

        @Size(max = 100, message = "Full name must be at most 100 characters")
        String fullName) {
}
