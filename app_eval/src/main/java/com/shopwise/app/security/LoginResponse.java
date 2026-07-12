package com.shopwise.app.security;

import java.time.Instant;
import java.util.List;

public class LoginResponse {
    private String token;
    private String tokenType;
    private Instant expiresAt;
    private String username;
    private List<String> roles;

    public LoginResponse(String token, String tokenType, Instant expiresAt,
            String username, List<String> roles) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiresAt = expiresAt;
        this.username = username;
        this.roles = roles;
    }

    public String getToken() { return token; }
    public String getTokenType() { return tokenType; }
    public Instant getExpiresAt() { return expiresAt; }
    public String getUsername() { return username; }
    public List<String> getRoles() { return roles; }
}
