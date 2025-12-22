package com.cubrain.springboot_starter_auth.global.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.cubrain.springboot_starter_auth.global.util.EnvironmentUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final EnvironmentUtil envUtil;

    // Handle when entity is not found
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(EntityNotFoundException e) {
        return createErrorResponse(HttpStatus.NOT_FOUND, "Resource not found.", e.getMessage());
    }

    // Handle when required parameter is missing
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(MissingServletRequestParameterException e) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Required parameter is missing.", e.getMessage());
    }

    // Handle validation failures for @Valid @RequestBody DTOs
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        org.springframework.validation.FieldError fieldError = e.getBindingResult().getFieldError();
        String errorMessage = fieldError != null ? fieldError.getDefaultMessage() : "Invalid input values.";
        return createErrorResponse(HttpStatus.BAD_REQUEST, errorMessage, e.getMessage());
    }

    // Handle external API call failures
    @ExceptionHandler({ HttpClientErrorException.class, WebClientResponseException.class })
    public ResponseEntity<Map<String, Object>> handleExternalApiError(Exception e) {
        log.error("External API call failed", e);
        return createErrorResponse(HttpStatus.BAD_REQUEST, "External API call failed.",
                "There was a problem connecting to the service.");
    }

    // Handle IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), null);
    }

    // Handle client disconnection during request processing
    @ExceptionHandler(ClientAbortException.class)
    public ResponseEntity<Map<String, Object>> handleClientAbort(ClientAbortException e) {
        log.warn("Client disconnected during request processing: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();
    }

    // Handle all other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        if (e instanceof ClientAbortException || e.getCause() instanceof ClientAbortException) {
            log.warn("Client abort exception in generic handler: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).build();
        }

        log.error("Unexpected error occurred", e);
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred.",
                e.getMessage());
    }

    // Common method to create error response
    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String message, String details) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);

        // Prevent exposing detailed information in non-local environments
        if (envUtil.isLocalEnvironment()) {
            errorResponse.put("details", details);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return ResponseEntity.status(status).headers(headers).body(errorResponse);
    }

    // Handle data integrity violation (e.g., unique constraint violation)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("Data integrity violation", e);
        return createErrorResponse(HttpStatus.CONFLICT, "Cannot perform request due to data integrity constraint.",
                "Integrity constraint violated.");
    }

    // Handle constraint violation
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation() {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Input value is invalid.",
                "Validation constraint violated.");
    }
}
