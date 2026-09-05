package com.kyawhsan.notiva.user.controller;

import com.kyawhsan.notiva.common.response.ApiResponse;
import com.kyawhsan.notiva.user.dto.AvatarResponse;
import com.kyawhsan.notiva.user.dto.ChangePasswordRequest;
import com.kyawhsan.notiva.user.dto.UpdateProfileRequest;
import com.kyawhsan.notiva.user.dto.UserProfileResponse;
import com.kyawhsan.notiva.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUserProfile() {
        UserProfileResponse response = userService.getCurrentUserProfile();

        return ResponseEntity
                .ok(ApiResponse.success("User profile retrieved successfully", response));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateCurrentUserProfile(
            @Valid
            @RequestBody
            UpdateProfileRequest request) {
        UserProfileResponse response = userService.updateCurrentUserProfile(request);

        return ResponseEntity
                .ok(ApiResponse.success("User profile updated successfully", response));
    }

    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AvatarResponse>> uploadAvatar(
            @RequestPart("file")
            MultipartFile file) {
        AvatarResponse response = userService.uploadAvatar(file);

        return ResponseEntity.ok(ApiResponse.success("Avatar uploaded successfully", response));
    }

    @DeleteMapping("/me/avatar")
    public ResponseEntity<ApiResponse<Void>> removeAvatar() {
        userService.removeAvatar();

        return ResponseEntity.ok(ApiResponse.success("Avatar removed successfully"));
    }

    @PatchMapping("/me/upgrade")
    public ResponseEntity<ApiResponse<UserProfileResponse>> upgradePlan() {
        UserProfileResponse response = userService.upgradePlan();

        return ResponseEntity
                .ok(ApiResponse.success("Plan upgraded to PREMIUM successfully", response));
    }

    @PatchMapping("/me/downgrade")
    public ResponseEntity<ApiResponse<UserProfileResponse>> downgradePlan() {
        UserProfileResponse response = userService.downgradePlan();

        return ResponseEntity
                .ok(ApiResponse.success("Plan downgraded to NORMAL successfully", response));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid
            @RequestBody
            ChangePasswordRequest request) {
        userService.changePassword(request);

        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteCurrentUserAccount() {
        userService.deleteCurrentUserAccount();

        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully"));
    }
}