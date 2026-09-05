package com.kyawhsan.notiva.service;

import com.kyawhsan.notiva.entity.User;
import com.kyawhsan.notiva.common.exception.UnauthorizedException;
import com.kyawhsan.notiva.repository.UserRepository;
import com.kyawhsan.notiva.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {

            throw new UnauthorizedException("Authentication is required");
        }

        return userRepository.findById(userDetails.getId()).orElseThrow(
                () -> new UnauthorizedException("Authenticated user no longer exists"));
    }
}