package com.kyawhsan.notiva.common.exception;

public class EmbeddingApiException extends RuntimeException {

    public EmbeddingApiException(
            String message) {
        super(message);
    }

    public EmbeddingApiException(
            String message,
            Throwable cause) {
        super(message, cause);
    }
}