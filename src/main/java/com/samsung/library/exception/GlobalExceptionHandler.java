package com.samsung.library.exception;

import com.samsung.library.dto.ApiResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Global Exception Handler for the Digital Library Management System
 * Provides centralized error handling with proper HTTP status codes and user-friendly messages
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        ex.getBindingResult().getGlobalErrors().forEach(error ->
                errors.put(error.getObjectName(), error.getDefaultMessage()));

        logger.warn("Validation failed for request {}: {}", request.getDescription(false), errors);

        ApiResponseDTO<Map<String, String>> response = new ApiResponseDTO<>();
        response.setSuccess(false);
        response.setMessage("Validation failed. Please check the provided data.");
        response.setData(errors);
        response.setTimestamp(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle constraint violations from method-level validations
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();

        for (ConstraintViolation<?> violation : violations) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(propertyPath, message);
        }

        logger.warn("Constraint violation for request {}: {}", request.getDescription(false), errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.error("Validation constraints violated"));
    }

    /**
     * Handle database integrity violations (duplicate keys, foreign key constraints, etc.)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, WebRequest request) {

        logger.error("Data integrity violation for request {}: {}",
                request.getDescription(false), ex.getMessage());

        String message = "Data integrity violation. ";
        String rootMessage = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();

        // Provide user-friendly messages for common constraint violations
        if (rootMessage.contains("Duplicate entry")) {
            if (rootMessage.contains("email")) {
                message += "Email address already exists.";
            } else if (rootMessage.contains("isbn")) {
                message += "ISBN already exists.";
            } else {
                message += "Duplicate value detected.";
            }
        } else if (rootMessage.contains("foreign key constraint")) {
            message += "Cannot complete operation due to related data dependencies.";
        } else if (rootMessage.contains("cannot be null")) {
            message += "Required field cannot be empty.";
        } else {
            message += "Please check your data and try again.";
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponseDTO.error(message));
    }

    /**
     * Handle business logic exceptions (custom RuntimeExceptions)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleBusinessLogicException(
            RuntimeException ex, WebRequest request) {

        logger.warn("Business logic exception for request {}: {}",
                request.getDescription(false), ex.getMessage());

        // Determine appropriate HTTP status based on exception message
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (ex.getMessage().toLowerCase().contains("not found")) {
            status = HttpStatus.NOT_FOUND;
        } else if (ex.getMessage().toLowerCase().contains("unauthorized") ||
                ex.getMessage().toLowerCase().contains("access denied")) {
            status = HttpStatus.FORBIDDEN;
        } else if (ex.getMessage().toLowerCase().contains("already exists") ||
                ex.getMessage().toLowerCase().contains("duplicate")) {
            status = HttpStatus.CONFLICT;
        }

        return ResponseEntity.status(status)
                .body(ApiResponseDTO.error(ex.getMessage()));
    }

    /**
     * Handle HTTP message not readable (invalid JSON, etc.)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {

        logger.warn("Invalid request body for request {}: {}",
                request.getDescription(false), ex.getMessage());

        String message = "Invalid request format. Please check your JSON data.";

        // Provide specific messages for common JSON errors
        if (ex.getMessage().contains("JSON parse error")) {
            message = "Invalid JSON format. Please check syntax.";
        } else if (ex.getMessage().contains("not a valid representation")) {
            message = "Invalid data type in request. Please check field types.";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.error(message));
    }

    /**
     * Handle method argument type mismatch (wrong parameter types)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {

        logger.warn("Method argument type mismatch for request {}: parameter '{}' should be of type '{}'",
                request.getDescription(false), ex.getName(), ex.getRequiredType().getSimpleName());

        String message = String.format("Invalid parameter '%s'. Expected type: %s",
                ex.getName(), ex.getRequiredType().getSimpleName());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.error(message));
    }

    /**
     * Handle missing request parameters
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, WebRequest request) {

        logger.warn("Missing request parameter for request {}: parameter '{}' of type '{}'",
                request.getDescription(false), ex.getParameterName(), ex.getParameterType());

        String message = String.format("Missing required parameter: '%s'", ex.getParameterName());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.error(message));
    }

    /**
     * Handle unsupported HTTP methods
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {

        String supportedMethods = ex.getSupportedHttpMethods() != null ?
                ex.getSupportedHttpMethods().stream()
                        .map(Enum::toString)
                        .collect(Collectors.joining(", ")) : "None";

        logger.warn("Unsupported HTTP method {} for request {}. Supported methods: {}",
                ex.getMethod(), request.getDescription(false), supportedMethods);

        String message = String.format("HTTP method '%s' not supported. Supported methods: %s",
                ex.getMethod(), supportedMethods);

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponseDTO.error(message));
    }

    /**
     * Handle 404 - No handler found
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleNoHandlerFound(
            NoHandlerFoundException ex, WebRequest request) {

        logger.warn("No handler found for {} {}", ex.getHttpMethod(), ex.getRequestURL());

        String message = String.format("Endpoint not found: %s %s", ex.getHttpMethod(), ex.getRequestURL());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponseDTO.error(message));
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        logger.warn("Illegal argument for request {}: {}", request.getDescription(false), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.error("Invalid argument: " + ex.getMessage()));
    }

    /**
     * Handle all other exceptions (fallback)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleGenericException(
            Exception ex, WebRequest request) {

        logger.error("Unexpected error for request {}: {}", request.getDescription(false), ex.getMessage(), ex);

        // Don't expose internal error details in production
        String message = "An unexpected error occurred. Please try again later.";

        // In development, provide more details
        String activeProfile = System.getProperty("spring.profiles.active", "");
        if ("dev".equals(activeProfile) || "development".equals(activeProfile)) {
            message += " Error: " + ex.getMessage();
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.error(message));
    }
}
