package com.kyawhsan.notiva.auth.dto;

import com.kyawhsan.notiva.user.entity.User;
import com.kyawhsan.notiva.user.enums.Role;
import com.kyawhsan.notiva.user.enums.UserPlan;

public record UserResponse(
        Long id,
        String email,
        String displayName,
        String avatarUrl,
        Role role,
        UserPlan plan,
        boolean emailVerified) {

    public static UserResponse from(
            User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(),
                user.getAvatarUrl(), user.getRole(), user.getPlan(), user.isEmailVerified());
    }
}