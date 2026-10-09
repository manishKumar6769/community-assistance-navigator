package com.navigator.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> badJson(HttpMessageNotReadableException e) {
        return build(HttpStatus.BAD_REQUEST, "Validation failed", "Request body is missing or not valid JSON");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> missingRequestParameter(MissingServletRequestParameterException e) {
        return build(HttpStatus.BAD_REQUEST, "Validation failed", "Required query parameter is missing");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> unexpected(Exception e) {
        log.error("Unexpected server error", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Server error", "Something went wrong. Please try again.");
    }

    private ResponseEntity<?> build(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "status", status.value(), "error", error, "message", message));
    }
}
