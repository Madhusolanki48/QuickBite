package com.quickbite.orderservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    record ApiError(Instant timestamp, int status, String error, String path) {
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> handle(NotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError(Instant.now(), 404, ex.getMessage(), request.getRequestURI()));
    }
}
