package com.kyawhsan.notiva.common.response;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiErrorResponse(
        boolean success,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors,
        LocalDateTime timestamp) {
}