package com.kyawhsan.notiva.auth.service;

import com.kyawhsan.notiva.auth.dto.RegisterRequest;
import com.kyawhsan.notiva.auth.dto.RegisterResponse;
import com.kyawhsan.notiva.auth.entity.AuthToken;
import com.kyawhsan.notiva.user.entity.User;
import com.kyawhsan.notiva.auth.enums.AuthTokenType;
import com.kyawhsan.notiva.user.enums.Role;
import com.kyawhsan.notiva.user.enums.UserPlan;
import com.kyawhsan.notiva.common.exception.BadRequestException;
import com.kyawhsan.notiva.common.exception.ConflictException;
import com.kyawhsan.notiva.auth.repository.AuthTokenRepository;
import com.kyawhsan.notiva.user.repository.UserRepository;
import com.kyawhsan.notiva.common.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.kyawhsan.notiva.auth.dto.LoginRequest;
import com.kyawhsan.notiva.auth.dto.LoginResponse;
import com.kyawhsan.notiva.auth.dto.UserResponse;
import com.kyawhsan.notiva.common.exception.UnauthorizedException;
import com.kyawhsan.notiva.security.JwtService;
import com.kyawhsan.notiva.security.CurrentUserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.kyawhsan.notiva.auth.dto.ForgotPasswordRequest;
import com.kyawhsan.notiva.auth.dto.ResetPasswordRequest;
import com.kyawhsan.notiva.auth.dto.VerifyResetOtpRequest;
import com.kyawhsan.notiva.auth.dto.RefreshTokenRequest;
import com.kyawhsan.notiva.auth.dto.RefreshTokenResponse;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenUtil tokenUtil;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;

    @Value("${app.auth.email-verification-expiration-hours}")
    private long verificationExpirationHours;

    @Value("${app.auth.password-reset-otp-expiration-minutes}")
    private long passwordResetOtpExpirationMinutes;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpirationMillis;

    @Transactional
    public RegisterResponse register(
            RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("An account already exists with this email");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName().trim());
        user.setRole(Role.USER);
        user.setPlan(UserPlan.NORMAL);
        user.setEmailVerified(false);
        user.setEnabled(true);

        userRepository.save(user);

        String rawToken = tokenUtil.generateVerificationToken();
        String hashedToken = tokenUtil.hash(rawToken);

        AuthToken authToken = new AuthToken();
        authToken.setToken(hashedToken);
        authToken.setType(AuthTokenType.EMAIL_VERIFICATION);
        authToken.setExpiresAt(LocalDateTime.now().plusHours(verificationExpirationHours));
        authToken.setUsed(false);
        authToken.setUser(user);

        authTokenRepository.save(authToken);

        emailService.sendVerificationEmail(user.getEmail(), user.getDisplayName(), rawToken);

        return new RegisterResponse(user.getId(), user.getEmail(), user.getDisplayName(),
                user.isEmailVerified());
    }

    @Transactional
    public void verifyEmail(
            String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new BadRequestException("Verification token is required");
        }

        String hashedToken = tokenUtil.hash(rawToken);

        AuthToken authToken = authTokenRepository
                .findByTokenAndType(hashedToken, AuthTokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new BadRequestException("Verification token is invalid"));

        if (authToken.isUsed()) {
            throw new BadRequestException("Verification token has already been used");
        }

        if (authToken.isExpired()) {
            throw new BadRequestException("Verification token has expired");
        }

        User user = authToken.getUser();

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email has already been verified");
        }

        user.setEmailVerified(true);
        authToken.markAsUsed();

        userRepository.save(user);
        authTokenRepository.save(authToken);
    }

    @Transactional
    public LoginResponse login(
            LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!user.isEmailVerified()) {
            throw new UnauthorizedException("Please verify your email before logging in");
        }

        if (!user.isEnabled()) {
            throw new UnauthorizedException("This account is disabled");
        }

        try {
            authenticationManager.authenticate(UsernamePasswordAuthenticationToken
                    .unauthenticated(normalizedEmail, request.password()));
        } catch (BadCredentialsException exception) {
            throw new UnauthorizedException("Invalid email or password");
        } catch (DisabledException exception) {
            throw new UnauthorizedException("This account is disabled");
        }

        String accessToken = jwtService
                .generateAccessToken(new com.kyawhsan.notiva.security.CustomUserDetails(user));
        String refreshToken = issueRefreshToken(user);

        return new LoginResponse(accessToken, refreshToken, "Bearer", jwtService.getExpiration() / 1000,
                UserResponse.from(user));
    }

    @Transactional
    public RefreshTokenResponse refresh(
            RefreshTokenRequest request) {
        AuthToken storedToken = authTokenRepository
                .findByTokenAndType(tokenUtil.hash(request.refreshToken()), AuthTokenType.REFRESH_TOKEN)
                .orElseThrow(() -> new UnauthorizedException("Refresh token is invalid, expired, or revoked"));

        if (!storedToken.isValid() || !storedToken.getUser().isEnabled()) {
            throw new UnauthorizedException("Refresh token is invalid, expired, or revoked");
        }

        storedToken.markAsUsed();
        authTokenRepository.save(storedToken);

        User user = storedToken.getUser();
        String accessToken = jwtService.generateAccessToken(
                new com.kyawhsan.notiva.security.CustomUserDetails(user));
        String refreshToken = issueRefreshToken(user);

        return new RefreshTokenResponse(accessToken, refreshToken, "Bearer",
                jwtService.getExpiration() / 1000);
    }

    @Transactional
    public void logout(
            RefreshTokenRequest request) {
        authTokenRepository.findByTokenAndType(tokenUtil.hash(request.refreshToken()),
                AuthTokenType.REFRESH_TOKEN).ifPresent(AuthToken::markAsUsed);
    }

    @Transactional
    public void forgotPassword(
            ForgotPasswordRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);

        if (user == null || !user.isEnabled()) {
            return;
        }

        authTokenRepository.deleteAllByUserAndType(user, AuthTokenType.PASSWORD_RESET_OTP);

        String rawOtp = tokenUtil.generateOtp();
        String hashedOtp = tokenUtil.hash(rawOtp);

        AuthToken authToken = new AuthToken();
        authToken.setToken(hashedOtp);
        authToken.setType(AuthTokenType.PASSWORD_RESET_OTP);
        authToken.setExpiresAt(LocalDateTime.now().plusMinutes(passwordResetOtpExpirationMinutes));
        authToken.setUsed(false);
        authToken.setUser(user);

        authTokenRepository.save(authToken);

        emailService.sendPasswordResetOtp(user.getEmail(), user.getDisplayName(), rawOtp);
    }

    @Transactional(readOnly = true)
    public void verifyResetOtp(
            VerifyResetOtpRequest request) {
        User user = findPasswordResetUser(request.email());

        AuthToken authToken = findValidResetOtp(user, request.otp());

        if (!authToken.isValid()) {
            throw new BadRequestException("OTP is invalid or expired");
        }
    }

    @Transactional
    public void resetPassword(
            ResetPasswordRequest request) {
        User user = findPasswordResetUser(request.email());

        AuthToken authToken = findValidResetOtp(user, request.otp());

        if (user.getPassword() != null
                && passwordEncoder.matches(request.newPassword(), user.getPassword())) {

            throw new BadRequestException(
                    "New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));

        authToken.markAsUsed();

        userRepository.save(user);
        authTokenRepository.save(authToken);
    }

    private User findPasswordResetUser(
            String email) {
        String normalizedEmail = normalizeEmail(email);

        return userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("OTP is invalid or expired"));
    }

    private AuthToken findValidResetOtp(
            User user,
            String rawOtp) {
        AuthToken authToken = authTokenRepository
                .findFirstByUserAndTypeAndUsedFalseOrderByCreatedAtDesc(user,
                        AuthTokenType.PASSWORD_RESET_OTP)
                .orElseThrow(() -> new BadRequestException("OTP is invalid or expired"));

        String hashedOtp = tokenUtil.hash(rawOtp);

        if (!authToken.getToken().equals(hashedOtp) || authToken.isExpired()
                || authToken.isUsed()) {

            throw new BadRequestException("OTP is invalid or expired");
        }

        return authToken;
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        return UserResponse.from(currentUserService.getCurrentUser());
    }

    private String normalizeEmail(
            String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String issueRefreshToken(
            User user) {
        String rawToken = tokenUtil.generateVerificationToken();

        AuthToken authToken = new AuthToken();
        authToken.setToken(tokenUtil.hash(rawToken));
        authToken.setType(AuthTokenType.REFRESH_TOKEN);
        authToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExpirationMillis / 1000));
        authToken.setUsed(false);
        authToken.setUser(user);

        authTokenRepository.save(authToken);

        return rawToken;
    }
}
