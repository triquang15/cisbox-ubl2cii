package com.cisbox.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * @author Tri Quang
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileValidationException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleFileValidationException(FileValidationException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, String>>> handleGenericException(Exception ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "An unexpected error occurred: " + ex.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }
}
