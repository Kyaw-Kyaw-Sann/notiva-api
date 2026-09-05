package com.kyawhsan.notiva.user.dto;

import com.kyawhsan.notiva.user.entity.User;
import com.kyawhsan.notiva.user.enums.Role;
import com.kyawhsan.notiva.user.enums.UserPlan;

import java.time.LocalDateTime;

public record UserProfileResponse(

        Long id,

        String email,

        String displayName,

        String avatarUrl,

        Role role,

        UserPlan plan,

        boolean emailVerified,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {

    public static UserProfileResponse from(
            User user) {
        return new UserProfileResponse(user.getId(), user.getEmail(), user.getDisplayName(),
                user.getAvatarUrl(), user.getRole(), user.getPlan(), user.isEmailVerified(),
                user.getCreatedAt(), user.getUpdatedAt());
    }
}