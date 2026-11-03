package com.fpt.authservice.dto;

/** Kết quả đăng nhập. Cùng các trường với backend hiện tại để FE dễ dùng lại. */
public record LoginResponse(
        String token,
        String tokenType,
        long expiresInSeconds,
        Long id,
        String username,
        String fullName,
        String email,
        String role) {

    public static LoginResponse bearer(String token, long expiresInSeconds, UserResponse user) {
        return new LoginResponse(token, "Bearer", expiresInSeconds, user.id(), user.username(),
                user.fullName(), user.email(), user.role());
    }
}
