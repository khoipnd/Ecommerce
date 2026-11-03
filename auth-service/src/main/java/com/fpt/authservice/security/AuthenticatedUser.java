package com.fpt.authservice.security;

/** Principal được đặt vào SecurityContext sau khi JWT hợp lệ: id tài khoản và role lấy từ token. */
public record AuthenticatedUser(Long id, String role) {
}
