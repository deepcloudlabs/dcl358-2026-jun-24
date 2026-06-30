package com.example.om.controller;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        var message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> "%s %s".formatted(error.getField(), error.getDefaultMessage()))
                .orElse("Request validation failed.");
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "Bad Request", message, request.getRequestURI()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(ApiError.of(400, "Bad Request", ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<ApiError> handleNotFound(NoSuchElementException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(404, "Not Found", ex.getMessage(), request.getRequestURI()));
    }
}
