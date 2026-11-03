package com.fpt.authservice.dto;

import com.fpt.authservice.entity.User;

/** Thông tin người dùng trả ra ngoài. Cố ý không có password/hash. */
public record UserResponse(Long id, String username, String fullName, String email, String role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getFullName(),
                user.getEmail(), user.getRole().getName());
    }
}
