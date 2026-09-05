package com.kyawhsan.notiva.common.exception;

import com.kyawhsan.notiva.common.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.BindException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponse> handleValidationException(
                        MethodArgumentNotValidException exception,
                        HttpServletRequest request) {
                Map<String, String> validationErrors = new LinkedHashMap<>();

                exception.getBindingResult().getFieldErrors()
                                .forEach(fieldError -> validationErrors.putIfAbsent(
                                                fieldError.getField(),
                                                fieldError.getDefaultMessage()));

                ApiErrorResponse response = new ApiErrorResponse(false,
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(), "Validation failed",
                                request.getRequestURI(), validationErrors, LocalDateTime.now());

                return ResponseEntity.badRequest().body(response);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleGeneralException(
                        Exception exception,
                        HttpServletRequest request) {
                ApiErrorResponse response = new ApiErrorResponse(false,
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                                "An unexpected error occurred", request.getRequestURI(), null,
                                LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        @ExceptionHandler(EmailSendingException.class)
        public ResponseEntity<ApiErrorResponse> handleEmailSendingException(
                        EmailSendingException exception,
                        HttpServletRequest request) {
                ApiErrorResponse response = new ApiErrorResponse(false,
                                HttpStatus.SERVICE_UNAVAILABLE.value(),
                                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                                exception.getMessage(), request.getRequestURI(), null,
                                LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }

        @ExceptionHandler(ConflictException.class)
        public ResponseEntity<ApiErrorResponse> handleConflictException(
                        ConflictException exception,
                        HttpServletRequest request) {
                ApiErrorResponse response = new ApiErrorResponse(false, HttpStatus.CONFLICT.value(),
                                HttpStatus.CONFLICT.getReasonPhrase(), exception.getMessage(),
                                request.getRequestURI(), null, LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ApiErrorResponse> handleBadRequestException(
                        BadRequestException exception,
                        HttpServletRequest request) {
                ApiErrorResponse response = new ApiErrorResponse(false,
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(), exception.getMessage(),
                                request.getRequestURI(), null, LocalDateTime.now());

                return ResponseEntity.badRequest().body(response);
        }

        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ApiErrorResponse> handleUnauthorizedException(
                        UnauthorizedException exception,
                        HttpServletRequest request) {
                ApiErrorResponse response = new ApiErrorResponse(false,
                                HttpStatus.UNAUTHORIZED.value(),
                                HttpStatus.UNAUTHORIZED.getReasonPhrase(), exception.getMessage(),
                                request.getRequestURI(), null, LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        @ExceptionHandler(InvalidImageException.class)
        public ResponseEntity<ApiErrorResponse> handleInvalidImageException(
                        InvalidImageException exception,
                        HttpServletRequest request) {
                ApiErrorResponse response = new ApiErrorResponse(false,
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(), exception.getMessage(),
                                request.getRequestURI(), null, LocalDateTime.now());

                return ResponseEntity.badRequest().body(response);
        }

        @ExceptionHandler(CloudinaryException.class)
        public ResponseEntity<ApiErrorResponse> handleCloudinaryException(
                        CloudinaryException exception,
                        HttpServletRequest request) {
                ApiErrorResponse response = new ApiErrorResponse(false,
                                HttpStatus.SERVICE_UNAVAILABLE.value(),
                                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                                exception.getMessage(), request.getRequestURI(), null,
                                LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
                        ResourceNotFoundException exception,
                        HttpServletRequest request) {
                ApiErrorResponse response = new ApiErrorResponse(false,
                                HttpStatus.NOT_FOUND.value(),
                                HttpStatus.NOT_FOUND.getReasonPhrase(), exception.getMessage(),
                                request.getRequestURI(), null, LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
                        MethodArgumentTypeMismatchException exception,
                        HttpServletRequest request) {
                String parameterName = exception.getName();

                String message = "Invalid value for parameter: " + parameterName;

                ApiErrorResponse response = new ApiErrorResponse(false,
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(), message,
                                request.getRequestURI(), null, LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        @ExceptionHandler(BindException.class)
        public ResponseEntity<ApiErrorResponse> handleBindException(
                        BindException exception,
                        HttpServletRequest request) {
                Map<String, String> validationErrors = new LinkedHashMap<>();

                exception.getBindingResult().getFieldErrors().forEach(error -> validationErrors
                                .put(error.getField(), error.getDefaultMessage()));

                ApiErrorResponse response = new ApiErrorResponse(false,
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(), "Validation failed",
                                request.getRequestURI(), validationErrors, LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        @ExceptionHandler(GroqTimeoutException.class)
        public ResponseEntity<ApiErrorResponse> handleGroqTimeout(
                        GroqTimeoutException exception,
                        HttpServletRequest request) {
                ApiErrorResponse response = new ApiErrorResponse(false,
                                HttpStatus.GATEWAY_TIMEOUT.value(),
                                HttpStatus.GATEWAY_TIMEOUT.getReasonPhrase(),
                                exception.getMessage(), request.getRequestURI(), null,
                                LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(response);
        }

        @ExceptionHandler(GroqApiException.class)
        public ResponseEntity<ApiErrorResponse> handleGroqApiException(
                        GroqApiException exception,
                        HttpServletRequest request) {
                ApiErrorResponse response = new ApiErrorResponse(false,
                                HttpStatus.SERVICE_UNAVAILABLE.value(),
                                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                                exception.getMessage(), request.getRequestURI(), null,
                                LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }

        @ExceptionHandler(AiUsageLimitException.class)
        public ResponseEntity<ApiErrorResponse> handleAiUsageLimit(
                        AiUsageLimitException exception,
                        HttpServletRequest request) {
                ApiErrorResponse response = new ApiErrorResponse(false,
                                HttpStatus.TOO_MANY_REQUESTS.value(),
                                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                                exception.getMessage(), request.getRequestURI(), null,
                                LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
        }

        @ExceptionHandler(EmbeddingTimeoutException.class)
        public ResponseEntity<ApiErrorResponse> handleEmbeddingTimeout(
                        EmbeddingTimeoutException exception,
                        HttpServletRequest request) {
                ApiErrorResponse response = new ApiErrorResponse(false,
                                HttpStatus.GATEWAY_TIMEOUT.value(),
                                HttpStatus.GATEWAY_TIMEOUT.getReasonPhrase(),
                                exception.getMessage(), request.getRequestURI(), null,
                                LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(response);
        }

        @ExceptionHandler(EmbeddingApiException.class)
        public ResponseEntity<ApiErrorResponse> handleEmbeddingApi(
                        EmbeddingApiException exception,
                        HttpServletRequest request) {
                ApiErrorResponse response = new ApiErrorResponse(false,
                                HttpStatus.SERVICE_UNAVAILABLE.value(),
                                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                                exception.getMessage(), request.getRequestURI(), null,
                                LocalDateTime.now());

                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        }
}
