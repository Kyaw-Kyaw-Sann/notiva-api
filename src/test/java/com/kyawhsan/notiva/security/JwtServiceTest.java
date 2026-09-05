package com.kyawhsan.notiva.security;

import com.kyawhsan.notiva.user.entity.User;
import com.kyawhsan.notiva.user.enums.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Test
    void acceptsOnlyUnexpiredAccessTokens() {
        JwtService jwtService = new JwtService(SECRET, 60_000);
        CustomUserDetails userDetails = userDetails();

        String accessToken = jwtService.generateAccessToken(userDetails);

        assertTrue(jwtService.isTokenValid(accessToken, userDetails));
    }

    @Test
    void rejectsJwtWithoutAccessTokenType() {
        JwtService jwtService = new JwtService(SECRET, 60_000);
        String tokenWithoutType = Jwts.builder().subject("user@example.com")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET))).compact();

        assertFalse(jwtService.isTokenValid(tokenWithoutType, userDetails()));
    }

    @Test
    void rejectsMalformedAndExpiredTokens() {
        JwtService jwtService = new JwtService(SECRET, 60_000);

        assertThrows(RuntimeException.class,
                () -> jwtService.isTokenValid("not-a-jwt", userDetails()));

        JwtService expiredJwtService = new JwtService(SECRET, -1);
        String expiredToken = expiredJwtService.generateAccessToken(userDetails());

        assertThrows(RuntimeException.class,
                () -> expiredJwtService.isTokenValid(expiredToken, userDetails()));
    }

    private CustomUserDetails userDetails() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPassword("encoded-password");
        user.setRole(Role.USER);
        user.setEnabled(true);

        return new CustomUserDetails(user);
    }
}
