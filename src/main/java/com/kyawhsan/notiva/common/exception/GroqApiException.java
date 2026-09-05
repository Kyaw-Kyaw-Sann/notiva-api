package com.kyawhsan.notiva.common.exception;

public class GroqApiException extends RuntimeException {

    private final int status;

    public GroqApiException(
            String message) {
        this(message, 503, null);
    }

    public GroqApiException(
            String message,
            Throwable cause) {
        this(message, 503, cause);
    }

    public GroqApiException(
            String message,
            int status,
            Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
