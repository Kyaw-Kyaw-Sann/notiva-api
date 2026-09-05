package com.kyawhsan.notiva.auth.controller;

import com.kyawhsan.notiva.common.response.ApiResponse;
import com.kyawhsan.notiva.auth.dto.RegisterRequest;
import com.kyawhsan.notiva.auth.dto.RegisterResponse;
import com.kyawhsan.notiva.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.kyawhsan.notiva.auth.dto.LoginRequest;
import com.kyawhsan.notiva.auth.dto.LoginResponse;
import com.kyawhsan.notiva.auth.dto.UserResponse;
import com.kyawhsan.notiva.auth.dto.ForgotPasswordRequest;
import com.kyawhsan.notiva.auth.dto.ResetPasswordRequest;
import com.kyawhsan.notiva.auth.dto.VerifyResetOtpRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid
            @RequestBody
            RegisterRequest request) {
        RegisterResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Registration successful. Check your email to verify your account.", response));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @RequestParam
            String token) {
        authService.verifyEmail(token);

        return ResponseEntity.ok(ApiResponse.success("Email verified successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid
            @RequestBody
            LoginRequest request) {
        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse response = authService.getCurrentUser();

        return ResponseEntity
                .ok(ApiResponse.success("Current user retrieved successfully", response));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid
            @RequestBody
            ForgotPasswordRequest request) {
        authService.forgotPassword(request);

        return ResponseEntity.ok(ApiResponse.success(
                "If an account exists with this email, a password reset OTP has been sent"));
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse<Void>> verifyResetOtp(
            @Valid
            @RequestBody
            VerifyResetOtpRequest request) {
        authService.verifyResetOtp(request);

        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid
            @RequestBody
            ResetPasswordRequest request) {
        authService.resetPassword(request);

        return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
    }
}