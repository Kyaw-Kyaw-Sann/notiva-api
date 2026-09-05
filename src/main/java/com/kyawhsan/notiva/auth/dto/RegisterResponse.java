package com.kyawhsan.notiva.auth.dto;

public record RegisterResponse(
        Long id,
        String email,
        String displayName,
        boolean emailVerified) {
}