package com.internship.kyc_system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler — converts exceptions into clean JSON error responses.
 *
 * The frontend reads these and shows them in the alert box.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Validation errors from @Valid — returns field-level messages */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        for (FieldError err : ex.getBindingResult().getFieldErrors()) {
            errors.put(err.getField(), err.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(errors);
    }

    /** KYC record not found */
    @ExceptionHandler(KycRecordNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(KycRecordNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage()));
    }

    /** File too large — Spring's multipart limit exceeded */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleFileTooLarge(MaxUploadSizeExceededException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", "File size exceeds the 5MB limit. Please upload a smaller file."));
    }

    /** Business logic errors (invalid status, invalid file type, etc.) */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", ex.getMessage()));
    }

    /** Catch-all for unexpected errors */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        // Don't expose internal error details to the client in production
        return ResponseEntity.internalServerError()
                .body(Map.of("message", "An unexpected error occurred. Please try again."));
    }
}