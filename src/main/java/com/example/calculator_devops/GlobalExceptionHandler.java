package com.example.calculator_devops;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Converts validation failures into a consistent HTTP 400 JSON body.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // e.g. /api/v1/add?a=5  (b missing)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex,
                                                            HttpServletRequest request) {
        String message = "Missing required parameter '" + ex.getParameterName() + "'";
        return ResponseEntity.badRequest().body(ErrorResponse.badRequest(message, request.getRequestURI()));
    }

    // e.g. /api/v1/add?a=abc&b=1
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest request) {
        String message = "Parameter '" + ex.getName() + "' must be a valid number, got '" + ex.getValue() + "'";
        return ResponseEntity.badRequest().body(ErrorResponse.badRequest(message, request.getRequestURI()));
    }

    // Division by zero and non-finite inputs
    @ExceptionHandler({ArithmeticException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleInvalidInput(RuntimeException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(ErrorResponse.badRequest(ex.getMessage(), request.getRequestURI()));
    }
}
