package com.kyawhsan.notiva.common.exception;

public class AiUsageLimitException extends RuntimeException {

    public AiUsageLimitException(
            String message) {
        super(message);
    }
}