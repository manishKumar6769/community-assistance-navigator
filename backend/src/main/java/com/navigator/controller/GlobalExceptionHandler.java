package com.navigator.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> badJson(HttpMessageNotReadableException e) {
        return build(HttpStatus.BAD_REQUEST, "Validation failed", "Request body is missing or not valid JSON");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> unexpected(Exception e) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Server error", "Something went wrong. Please try again.");
    }

    private ResponseEntity<?> build(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "status", status.value(), "error", error, "message", message));
    }
}