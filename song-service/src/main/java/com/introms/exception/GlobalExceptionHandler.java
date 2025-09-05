package com.introms.exception;

import com.introms.exception.response.SimpleErrorResponse;
import com.introms.exception.response.ValidationErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class) // Custom exception
    public ResponseEntity<SimpleErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        SimpleErrorResponse errorResponse = new SimpleErrorResponse(
                ex.getMessage(),
                String.valueOf(HttpStatus.NOT_FOUND.value())
        );
        log.error("Resource not found:{}",ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IdValidationException.class) // Custom exception
    public ResponseEntity<SimpleErrorResponse> handleValidation(IdValidationException ex) {
        SimpleErrorResponse errorResponse = new SimpleErrorResponse(
                ex.getMessage(),
                String.valueOf(HttpStatus.BAD_REQUEST.value())
        );

        log.error("Resource validation exception:{}",ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidIdCsvException.class) // InvalidIdCsvException
    public ResponseEntity<SimpleErrorResponse> handleIdCsv(InvalidIdCsvException ex) {
        SimpleErrorResponse errorResponse = new SimpleErrorResponse(
                ex.getMessage(),
                String.valueOf(HttpStatus.BAD_REQUEST.value())
        );

        log.error("InvalidIdCsv data:{}",ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SongMetadataAlreadyExistException.class) // Custom exception
    public ResponseEntity<SimpleErrorResponse> handleSongMetadataAlreadyExist(SongMetadataAlreadyExistException ex) {
        SimpleErrorResponse errorResponse = new SimpleErrorResponse(
                ex.getMessage(),
                String.valueOf(HttpStatus.CONFLICT.value())
        );

        log.error("SongMetadata already exists:{}",ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MetadataValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleMetadataValidations(MetadataValidationException ex) {
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                "Validation error",
                ex.getDetails(),
                String.valueOf(HttpStatus.BAD_REQUEST.value())
        );
        log.error("Validation error:{}",ex.getDetails());
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
        log.error("Unknown error",ex);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}