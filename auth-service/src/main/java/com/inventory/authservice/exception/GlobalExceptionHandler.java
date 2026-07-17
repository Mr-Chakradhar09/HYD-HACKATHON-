package com.inventory.authservice.exception;

import com.inventory.authservice.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserAlreadyExistsException(
            UserAlreadyExistsException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                null,
                request.getRequestURI()
        );
    }
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request) {

        ApiResponse<Object> response = new ApiResponse<>();

        response.setSuccess(false);
        response.setStatus(HttpStatus.NOT_FOUND.value());
        response.setMessage(ex.getMessage());
        response.setData(null);
        response.setPath(request.getRequestURI());
        response.setTimestamp(LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }
    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiResponse<Object>> handlePassword(
            InvalidPasswordException ex,
            HttpServletRequest request) {

        ApiResponse<Object> response = new ApiResponse<>();

        response.setSuccess(false);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setMessage(ex.getMessage());
        response.setData(null);
        response.setPath(request.getRequestURI());
        response.setTimestamp(LocalDateTime.now());

        return ResponseEntity
                .badRequest()
                .body(response);
    }
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ApiResponse<Map<String,String>>> handleValidation(
//            MethodArgumentNotValidException ex,
//            HttpServletRequest request) {
//
//        Map<String,String> errors = new LinkedHashMap<>();
//
//        ex.getBindingResult()
//                .getFieldErrors()
//                .forEach(error ->
//                        errors.put(
//                                error.getField(),
//                                error.getDefaultMessage()
//                        ));
//
//        ApiResponse<Map<String,String>> response =
//                new ApiResponse<>();
//
//        response.setSuccess(false);
//        response.setStatus(HttpStatus.BAD_REQUEST.value());
//        response.setMessage("Validation failed");
//        response.setData(errors);
//        response.setPath(request.getRequestURI());
//        response.setTimestamp(LocalDateTime.now());
//
//        return ResponseEntity
//                .badRequest()
//                .body(response);
//    }

//    @ExceptionHandler(UserAlreadyExistsException.class)
//    public ResponseEntity<ApiResponse<Object>> handleUserExists(
//            UserAlreadyExistsException ex,
//            HttpServletRequest request) {
//
//        ApiResponse<Object> response = new ApiResponse<>();
//
//        response.setSuccess(false);
//        response.setStatus(HttpStatus.CONFLICT.value());
//        response.setMessage(ex.getMessage());
//        response.setData(null);
//        response.setPath(request.getRequestURI());
//        response.setTimestamp(LocalDateTime.now());
//
//        return ResponseEntity
//                .status(HttpStatus.CONFLICT)
//                .body(response);
//    }
@ExceptionHandler(BadCredentialsException.class)
public ResponseEntity<ApiResponse<Object>> handleBadCredentials(
        BadCredentialsException ex,
        HttpServletRequest request) {

    return buildResponse(
            HttpStatus.UNAUTHORIZED,
            "Invalid employee code or password",
            null,
            request.getRequestURI()
    );
}
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidCredentialsException(
            InvalidCredentialsException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                null,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                null,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidTokenException(
            InvalidTokenException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                null,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleEmployeeNotFound(
            EmployeeNotFoundException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                null,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                null,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                errors,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                null,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.FORBIDDEN,
                "Access Denied",
                null,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(
            Exception ex,
            HttpServletRequest request) {

        ApiResponse<Object> response = new ApiResponse<>();

        response.setSuccess(false);
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setMessage(ex.getMessage());
        response.setData(null);
        response.setPath(request.getRequestURI());
        response.setTimestamp(LocalDateTime.now());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    private ResponseEntity<ApiResponse<Object>> buildResponse(
            HttpStatus status,
            String message,
            Object data,
            String path) {

        ApiResponse<Object> response = new ApiResponse<>();

        response.setSuccess(status.is2xxSuccessful());
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(LocalDateTime.now());
        response.setPath(path);
        response.setStatus(status.value());

        return new ResponseEntity<>(response, status);
    }
}