package com.kyawhsan.notiva.admin.dto;

import com.kyawhsan.notiva.entity.User;
import com.kyawhsan.notiva.enums.Role;
import com.kyawhsan.notiva.enums.UserPlan;
import java.time.LocalDateTime;

public record AdminUserResponse(Long id, String email, String displayName, String avatarUrl,
        Role role, UserPlan plan, boolean enabled, boolean emailVerified, LocalDateTime createdAt) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(user.getId(), user.getEmail(), user.getDisplayName(),
                user.getAvatarUrl(), user.getRole(), user.getPlan(), user.isEnabled(),
                user.isEmailVerified(), user.getCreatedAt());
    }
}
