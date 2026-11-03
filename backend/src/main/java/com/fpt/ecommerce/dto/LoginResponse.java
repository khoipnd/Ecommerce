package com.fpt.ecommerce.dto;

public class LoginResponse {

    private final String token;
    private final String tokenType = "Bearer";
    private final long expiresInSeconds;
    private final Long id;
    private final String username;
    private final String fullName;
    private final String email;
    private final String role;

    public LoginResponse(String token, long expiresInSeconds, Long id, String username,
                         String fullName, String email, String role) {
        this.token = token;
        this.expiresInSeconds = expiresInSeconds;
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public String getToken() { return token; }
    public String getTokenType() { return tokenType; }
    public long getExpiresInSeconds() { return expiresInSeconds; }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
