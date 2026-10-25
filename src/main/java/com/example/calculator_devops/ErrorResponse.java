package com.example.calculator_devops;

import java.time.Instant;

/**
 * Structured error body returned for every HTTP 400.
 */
public record ErrorResponse(Instant timestamp, int status, String error, String message, String path) {

    public static ErrorResponse badRequest(String message, String path) {
        return new ErrorResponse(Instant.now(), 400, "Bad Request", message, path);
    }
}
