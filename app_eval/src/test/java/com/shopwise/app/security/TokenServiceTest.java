package com.shopwise.app.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class TokenServiceTest {
    @Test
    void issuedTokenRestoresAuthenticatedPrincipalAndRole() {
        SecurityProperties properties = new SecurityProperties();
        properties.setTokenTtl(Duration.ofHours(1));
        TokenService service = new TokenService(properties);
        var source = new UsernamePasswordAuthenticationToken("merchant", null,
                List.of(new SimpleGrantedAuthority("ROLE_MERCHANT")));

        var issued = service.issue(source);
        var restored = service.authenticate(issued.getValue());

        assertThat(restored).isPresent();
        assertThat(restored.orElseThrow().getName()).isEqualTo("merchant");
        assertThat(restored.orElseThrow().getAuthorities())
                .extracting(Object::toString).containsExactly("ROLE_MERCHANT");
    }

    @Test
    void expiredTokenIsRejected() {
        SecurityProperties properties = new SecurityProperties();
        properties.setTokenTtl(Duration.ofSeconds(-1));
        TokenService service = new TokenService(properties);
        var source = new UsernamePasswordAuthenticationToken("merchant", null, List.of());

        var issued = service.issue(source);

        assertThat(service.authenticate(issued.getValue())).isEmpty();
    }
}
