package com.inventory.replenishmentservice.exception;

import com.inventory.replenishmentservice.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ApiResponse<>(false, ex.getMessage(), null, 404), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicate(DuplicateResourceException ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ApiResponse<>(false, ex.getMessage(), null, 409), HttpStatus.CONFLICT);
    }
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidOp(InvalidOperationException ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ApiResponse<>(false, ex.getMessage(), null, 400), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        ApiResponse<Object> response = new ApiResponse<>(false, "Validation failed", errors, 400);
        return ResponseEntity.badRequest().body(response);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ApiResponse<>(false, ex.getMessage(), null, 500), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
