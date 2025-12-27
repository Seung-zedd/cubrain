package com.cubrain.springboot_starter_auth.global.exception;

import com.cubrain.springboot_starter_auth.global.util.EnvironmentUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import jakarta.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.REQUEST_TIMEOUT;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final EnvironmentUtil envUtil;

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleEntityNotFound(EntityNotFoundException e) {
        return createErrorResponse(NOT_FOUND, "Resource not found.", e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDto> handleMissingParameter(MissingServletRequestParameterException e) {
        return createErrorResponse(BAD_REQUEST, "Required parameter is missing.", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String errorMessage = fieldError != null ? fieldError.getDefaultMessage() : "Invalid input values.";
        return createErrorResponse(BAD_REQUEST, errorMessage, e.getMessage());
    }

    @ExceptionHandler({ HttpClientErrorException.class, WebClientResponseException.class })
    public ResponseEntity<ErrorResponseDto> handleExternalApiError(Exception e) {
        log.error("External API call failed", e);
        return createErrorResponse(BAD_REQUEST, "External API call failed.",
                "There was a problem connecting to the service.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException e) {
        return createErrorResponse(BAD_REQUEST, e.getMessage(), null);
    }

    @ExceptionHandler(ClientAbortException.class)
    public ResponseEntity<ErrorResponseDto> handleClientAbort(ClientAbortException e) {
        log.warn("Client disconnected during request processing: {}", e.getMessage());
        return ResponseEntity.status(REQUEST_TIMEOUT).build();
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNoResourceFound(NoResourceFoundException e) {
        log.debug("Resource not found: {}", e.getMessage());
        return createErrorResponse(NOT_FOUND, "Resource not found.", e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodNotSupported(HttpRequestMethodNotSupportedException e,
            HttpServletRequest request) {
        log.warn("Method not supported: {} {} - {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return createErrorResponse(METHOD_NOT_ALLOWED, "HTTP method not supported.", e.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponseDto> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        return createErrorResponse(UNSUPPORTED_MEDIA_TYPE, "Content type not supported.", e.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorResponseDto> handleMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException e) {
        return createErrorResponse(NOT_ACCEPTABLE, "Acceptable media type not found.", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception e, HttpServletRequest request) {
        if (e instanceof ClientAbortException || e.getCause() instanceof ClientAbortException) {
            log.warn("Client abort exception in generic handler: {}", e.getMessage());
            return ResponseEntity.status(REQUEST_TIMEOUT).build();
        }

        log.error("Unexpected error occurred at [{} {}]: ", request.getMethod(), request.getRequestURI(), e);
        return createErrorResponse(INTERNAL_SERVER_ERROR, "An internal server error occurred.",
                e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("Data integrity violation", e);
        return createErrorResponse(CONFLICT, "Cannot perform request due to data integrity constraint.",
                "Integrity constraint violated.");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolation() {
        return createErrorResponse(BAD_REQUEST, "Input value is invalid.",
                "Validation constraint violated.");
    }

    private ResponseEntity<ErrorResponseDto> createErrorResponse(HttpStatus status, String message, String details) {
        ErrorResponseDto errorResponse = ErrorResponseDto.of(
                status.value(),
                status.getReasonPhrase(),
                message,
                envUtil.isLocalEnvironment() ? details : null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);

        return ResponseEntity.status(status).headers(headers).body(errorResponse);
    }
}
