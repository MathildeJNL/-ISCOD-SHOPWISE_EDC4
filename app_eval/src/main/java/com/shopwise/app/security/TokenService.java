package com.shopwise.app.security;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private final ConcurrentMap<String, TokenSession> sessions = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private final SecurityProperties properties;

    public TokenService(SecurityProperties properties) {
        this.properties = properties;
    }

    public IssuedToken issue(Authentication authentication) {
        // Le token contient 32 octets aléatoires et ne contient aucune donnée utilisateur.
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String value = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        Instant expiresAt = Instant.now().plus(properties.getTokenTtl());
        List<String> authorities = new ArrayList<>();
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            authorities.add(authority.getAuthority());
        }
        sessions.put(value, new TokenSession(authentication.getName(), authorities, expiresAt));
        return new IssuedToken(value, expiresAt);
    }

    public Optional<Authentication> authenticate(String token) {
        // On cherche le token puis on vérifie sa date d'expiration.
        TokenSession session = sessions.get(token);
        if (session == null) {
            return Optional.empty();
        }
        if (!session.getExpiresAt().isAfter(Instant.now())) {
            sessions.remove(token);
            return Optional.empty();
        }
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (String authority : session.getAuthorities()) {
            authorities.add(new SimpleGrantedAuthority(authority));
        }
        return Optional.of(new UsernamePasswordAuthenticationToken(
                session.getUsername(), token, authorities));
    }

    private static class TokenSession {
        private final String username;
        private final List<String> authorities;
        private final Instant expiresAt;

        TokenSession(String username, List<String> authorities, Instant expiresAt) {
            this.username = username;
            this.authorities = authorities;
            this.expiresAt = expiresAt;
        }

        String getUsername() { return username; }
        List<String> getAuthorities() { return authorities; }
        Instant getExpiresAt() { return expiresAt; }
    }

    public static class IssuedToken {
        private final String value;
        private final Instant expiresAt;

        IssuedToken(String value, Instant expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }

        public String getValue() { return value; }
        public Instant getExpiresAt() { return expiresAt; }
    }
}
