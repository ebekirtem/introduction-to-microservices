package com.introms.exception;

import com.introms.exception.response.SimpleErrorResponse;
import com.introms.exception.response.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class) // Custom exception
    public ResponseEntity<SimpleErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        SimpleErrorResponse errorResponse = new SimpleErrorResponse(
                ex.getMessage(),
                "404" // Error code for NOT_FOUND
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class) // Custom exception
    public ResponseEntity<SimpleErrorResponse> handleBadRequest(BadRequestException ex) {
        SimpleErrorResponse errorResponse = new SimpleErrorResponse(
                ex.getMessage(),
                "400" // Error code for NOT_FOUND
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle validation exceptions for @Valid.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> details = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.put(error.getField(), error.getDefaultMessage());
        }

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                "Validation error",
                details,
                "400" // Error code for BAD_REQUEST
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle any other unhandled exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<SimpleErrorResponse> handleGeneralException(Exception ex) {
        SimpleErrorResponse errorResponse = new SimpleErrorResponse(
                "An unexpected error occurred: " + ex.getMessage(),
                "500" // Error code for INTERNAL_SERVER_ERROR
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}