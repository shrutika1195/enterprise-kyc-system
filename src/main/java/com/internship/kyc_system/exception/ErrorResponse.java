package com.internship.kyc_system.exception;

import java.time.LocalDateTime;

public class ErrorResponse {
    private int statusCode;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponse(int statusCode, String message, LocalDateTime timestamp) {
        this.statusCode = statusCode;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getters
    public int getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}