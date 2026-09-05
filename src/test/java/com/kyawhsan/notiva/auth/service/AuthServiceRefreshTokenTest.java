package com.kyawhsan.notiva.auth.service;

import com.kyawhsan.notiva.auth.dto.RefreshTokenRequest;
import com.kyawhsan.notiva.auth.dto.RefreshTokenResponse;
import com.kyawhsan.notiva.auth.entity.AuthToken;
import com.kyawhsan.notiva.auth.enums.AuthTokenType;
import com.kyawhsan.notiva.auth.repository.AuthTokenRepository;
import com.kyawhsan.notiva.common.exception.UnauthorizedException;
import com.kyawhsan.notiva.common.util.TokenUtil;
import com.kyawhsan.notiva.entity.User;
import com.kyawhsan.notiva.enums.Role;
import com.kyawhsan.notiva.repository.UserRepository;
import com.kyawhsan.notiva.security.JwtService;
import com.kyawhsan.notiva.service.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceRefreshTokenTest {

    private static final String SECRET = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Mock private UserRepository userRepository;
    @Mock private AuthTokenRepository authTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenUtil tokenUtil;
    @Mock private EmailService emailService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private CurrentUserService currentUserService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, authTokenRepository, passwordEncoder,
                tokenUtil, emailService, authenticationManager, new JwtService(SECRET, 60_000),
                currentUserService);
        ReflectionTestUtils.setField(authService, "refreshExpirationMillis", 3_600_000L);
    }

    @Test
    void refreshRotatesAndRevokesThePresentedToken() {
        User user = enabledUser();
        AuthToken existingToken = refreshToken(user, LocalDateTime.now().plusHours(1), false);
        when(tokenUtil.hash("old-refresh")).thenReturn("old-hash");
        when(tokenUtil.generateVerificationToken()).thenReturn("new-refresh");
        when(tokenUtil.hash("new-refresh")).thenReturn("new-hash");
        when(authTokenRepository.findByTokenAndType("old-hash", AuthTokenType.REFRESH_TOKEN))
                .thenReturn(Optional.of(existingToken));

        RefreshTokenResponse response = authService.refresh(new RefreshTokenRequest("old-refresh"));

        assertTrue(existingToken.isUsed());
        assertEquals("new-refresh", response.refreshToken());
        assertEquals("Bearer", response.tokenType());
        assertFalse(response.accessToken().isBlank());
        verify(authTokenRepository, times(2)).save(any(AuthToken.class));
    }

    @Test
    void rejectsExpiredOrRevokedRefreshTokens() {
        AuthToken expiredToken = refreshToken(enabledUser(), LocalDateTime.now().minusSeconds(1), false);
        when(tokenUtil.hash("expired")).thenReturn("expired-hash");
        when(authTokenRepository.findByTokenAndType("expired-hash", AuthTokenType.REFRESH_TOKEN))
                .thenReturn(Optional.of(expiredToken));

        assertThrows(UnauthorizedException.class,
                () -> authService.refresh(new RefreshTokenRequest("expired")));

        AuthToken revokedToken = refreshToken(enabledUser(), LocalDateTime.now().plusHours(1), true);
        when(tokenUtil.hash("revoked")).thenReturn("revoked-hash");
        when(authTokenRepository.findByTokenAndType("revoked-hash", AuthTokenType.REFRESH_TOKEN))
                .thenReturn(Optional.of(revokedToken));

        assertThrows(UnauthorizedException.class,
                () -> authService.refresh(new RefreshTokenRequest("revoked")));
    }

    @Test
    void rejectsAccessTokensAtTheRefreshEndpointAndLogoutRevokesRefreshTokens() {
        when(tokenUtil.hash("access-jwt")).thenReturn("access-hash");
        when(authTokenRepository.findByTokenAndType("access-hash", AuthTokenType.REFRESH_TOKEN))
                .thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class,
                () -> authService.refresh(new RefreshTokenRequest("access-jwt")));

        AuthToken refreshToken = refreshToken(enabledUser(), LocalDateTime.now().plusHours(1), false);
        when(tokenUtil.hash("refresh")).thenReturn("refresh-hash");
        when(authTokenRepository.findByTokenAndType("refresh-hash", AuthTokenType.REFRESH_TOKEN))
                .thenReturn(Optional.of(refreshToken));

        authService.logout(new RefreshTokenRequest("refresh"));

        assertTrue(refreshToken.isUsed());
    }

    private User enabledUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPassword("encoded-password");
        user.setRole(Role.USER);
        user.setEnabled(true);
        return user;
    }

    private AuthToken refreshToken(User user, LocalDateTime expiresAt, boolean used) {
        AuthToken token = new AuthToken();
        token.setUser(user);
        token.setType(AuthTokenType.REFRESH_TOKEN);
        token.setExpiresAt(expiresAt);
        token.setUsed(used);
        return token;
    }
}
