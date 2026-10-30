package com.healthskillconnect.controller;

import java.util.Map;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> invalid(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(
                Map.of("message", "Please check the submitted fields.")
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Map<String, String>> status(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(
                Map.of(
                        "message",
                        e.getReason() == null ? "Request could not be processed." : e.getReason()
                )
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> invalidRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(
                Map.of(
                        "message",
                        e.getMessage() == null ? "Invalid request." : e.getMessage()
                )
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<Map<String, String>> unavailable(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                Map.of(
                        "message",
                        e.getMessage() == null ? "A dependent service is unavailable." : e.getMessage()
                )
        );
    }

    @ExceptionHandler(NoSuchElementException.class)
    ResponseEntity<Map<String, String>> missing(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "message",
                        e.getMessage() == null ? "The requested item was not found." : e.getMessage()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, String>> unexpected(Exception e) {
        log.error("Unhandled API error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("message", "The request could not be processed.")
        );
    }
}
