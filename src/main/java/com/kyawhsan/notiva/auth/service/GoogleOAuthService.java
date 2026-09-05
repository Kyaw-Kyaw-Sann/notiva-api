package com.kyawhsan.notiva.auth.service;

import com.kyawhsan.notiva.user.entity.User;
import com.kyawhsan.notiva.user.enums.Role;
import com.kyawhsan.notiva.user.enums.UserPlan;
import com.kyawhsan.notiva.common.exception.BadRequestException;
import com.kyawhsan.notiva.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    private final UserRepository userRepository;

    @Transactional
    public User processGoogleUser(
            String googleId,
            String email,
            String displayName,
            String avatarUrl) {
        if (googleId == null || email == null) {
            throw new BadRequestException("Google account information is incomplete");
        }

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElseGet(
                () -> createGoogleUser(googleId, normalizedEmail, displayName, avatarUrl));

        if (user.getGoogleId() != null && !user.getGoogleId().equals(googleId)) {

            throw new BadRequestException("This email is linked to another Google account");
        }

        if (user.getGoogleId() == null) {
            user.setGoogleId(googleId);
        }

        user.setEmailVerified(true);

        if (avatarUrl != null && !avatarUrl.isBlank()) {
            user.setAvatarUrl(avatarUrl);
        }

        return userRepository.save(user);
    }

    private User createGoogleUser(
            String googleId,
            String email,
            String displayName,
            String avatarUrl) {
        User user = new User();

        user.setEmail(email);
        user.setPassword(null);
        user.setDisplayName(resolveDisplayName(displayName, email));
        user.setAvatarUrl(avatarUrl);
        user.setGoogleId(googleId);
        user.setRole(Role.USER);
        user.setPlan(UserPlan.NORMAL);
        user.setEmailVerified(true);
        user.setEnabled(true);

        return user;
    }

    private String resolveDisplayName(
            String displayName,
            String email) {
        if (displayName != null && !displayName.isBlank()) {
            return displayName.trim();
        }

        return email.substring(0, email.indexOf("@"));
    }
}