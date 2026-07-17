package com.inventory.warehouseservice.exception;

import com.inventory.warehouseservice.dto.response.ApiResponse;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WarehouseNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleWarehouseNotFoundException(
            WarehouseNotFoundException ex,
            HttpServletRequest request) {

        return buildResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(WarehouseAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleWarehouseAlreadyExistsException(
            WarehouseAlreadyExistsException ex,
            HttpServletRequest request) {

        return buildResponse(
                ex.getMessage(),
                HttpStatus.CONFLICT,
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(EmployeeAlreadyAssignedException.class)
    public ResponseEntity<ApiResponse<Object>> handleEmployeeAlreadyAssignedException(
            EmployeeAlreadyAssignedException ex,
            HttpServletRequest request) {

        return buildResponse(
                ex.getMessage(),
                HttpStatus.CONFLICT,
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(EmployeeInactiveException.class)
    public ResponseEntity<ApiResponse<Object>> handleEmployeeInactiveException(
            EmployeeInactiveException ex,
            HttpServletRequest request) {

        return buildResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(InvalidWarehouseRoleException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidWarehouseRoleException(
            InvalidWarehouseRoleException ex,
            HttpServletRequest request) {

        return buildResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(InventoryManagerAlreadyAssignedException.class)
    public ResponseEntity<ApiResponse<Object>> handleInventoryManagerAlreadyAssignedException(
            InventoryManagerAlreadyAssignedException ex,
            HttpServletRequest request) {

        return buildResponse(
                ex.getMessage(),
                HttpStatus.CONFLICT,
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(WarehouseAssignmentNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleWarehouseAssignmentNotFoundException(
            WarehouseAssignmentNotFoundException ex,
            HttpServletRequest request) {

        return buildResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        return buildResponse(
                ex.getMessage(),
                HttpStatus.NOT_FOUND,
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateResource(
            DuplicateResourceException ex,
            HttpServletRequest request) {

        return buildResponse(
                ex.getMessage(),
                HttpStatus.CONFLICT,
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidOperation(
            InvalidOperationException ex,
            HttpServletRequest request) {

        return buildResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage()));

        return buildResponse(
                "Validation failed",
                HttpStatus.BAD_REQUEST,
                request.getRequestURI(),
                errors
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        return buildResponse(
                "Access Denied",
                HttpStatus.FORBIDDEN,
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        return buildResponse(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST,
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        return buildResponse(
                "Malformed request body",
                HttpStatus.BAD_REQUEST,
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {

        return buildResponse(
                "Method not allowed",
                HttpStatus.METHOD_NOT_ALLOWED,
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiResponse<Object>> handleFeignException(
            FeignException ex,
            HttpServletRequest request) {

        // 404 from auth-service means employee/resource not found
        if (ex.status() == 404) {
            return buildResponse(
                    "Referenced resource not found: " + ex.getMessage(),
                    HttpStatus.BAD_REQUEST,
                    request.getRequestURI(),
                    null
            );
        }
        return buildResponse(
                "Service communication error",
                HttpStatus.SERVICE_UNAVAILABLE,
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(
            Exception ex,
            HttpServletRequest request) {

        return buildResponse(
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request.getRequestURI(),
                null
        );
    }

    private ResponseEntity<ApiResponse<Object>> buildResponse(
            String message,
            HttpStatus status,
            String path,
            Object data) {

        ApiResponse<Object> response = new ApiResponse<>();

        response.setSuccess(false);
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(LocalDateTime.now());
        response.setPath(path);
        response.setStatus(status.value());

        return ResponseEntity
                .status(status)
                .body(response);
    }
}