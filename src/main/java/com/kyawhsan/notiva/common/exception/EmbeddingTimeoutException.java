package com.kyawhsan.notiva.common.exception;

public class EmbeddingTimeoutException extends RuntimeException {

    public EmbeddingTimeoutException(
            String message,
            Throwable cause) {
        super(message, cause);
    }
}