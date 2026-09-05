package com.kyawhsan.notiva.common.exception;

public class GroqTimeoutException extends RuntimeException {

    public GroqTimeoutException(
            String message,
            Throwable cause) {
        super(message, cause);
    }
}