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
                String.valueOf(HttpStatus.NOT_FOUND.value())
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidMp3Exception.class) // Custom exception
    public ResponseEntity<SimpleErrorResponse> handleInvalidMp3Request(InvalidMp3Exception ex) {
        SimpleErrorResponse errorResponse = new SimpleErrorResponse(
                ex.getMessage(),
                String.valueOf(HttpStatus.BAD_REQUEST.value())
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidIdCsvException.class) // InvalidIdCsvException
    public ResponseEntity<SimpleErrorResponse> handleIdCsv(InvalidIdCsvException ex) {
        SimpleErrorResponse errorResponse = new SimpleErrorResponse(
                ex.getMessage(),
                String.valueOf(HttpStatus.BAD_REQUEST.value())
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SongMetadataAlreadyExistException.class) // Custom exception
    public ResponseEntity<SimpleErrorResponse> handleSongMetadataAlreadyExist(SongMetadataAlreadyExistException ex) {
        SimpleErrorResponse errorResponse = new SimpleErrorResponse(
                ex.getMessage(),
                String.valueOf(HttpStatus.CONFLICT.value())
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidations(ValidationException ex) {
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                "Validation error",
                ex.getDetails(),
                String.valueOf(HttpStatus.BAD_REQUEST.value())
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
                String.valueOf(HttpStatus.BAD_REQUEST.value())
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle any other unhandled exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<SimpleErrorResponse> handleGeneralException(Exception ex) {
        SimpleErrorResponse errorResponse = new SimpleErrorResponse(
                "An error occurred on the server: ",
                String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}