package com.introms.exception;

import com.introms.exception.response.SimpleErrorResponse;
import com.introms.exception.response.ValidationErrorResponse;
import org.apache.coyote.BadRequestException;
import org.apache.juli.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class) // Custom exception
    public ResponseEntity<SimpleErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        SimpleErrorResponse errorResponse = new SimpleErrorResponse(
                ex.getMessage(),
                String.valueOf(HttpStatus.NOT_FOUND.value())
        );
        LOGGER.error("Resource not found:{}",ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidMp3Exception.class) // Custom exception
    public ResponseEntity<SimpleErrorResponse> handleInvalidMp3Request(InvalidMp3Exception ex) {
        SimpleErrorResponse errorResponse = new SimpleErrorResponse(
                ex.getMessage(),
                String.valueOf(HttpStatus.BAD_REQUEST.value())
        );

        LOGGER.error("InvalidMp3 data:{}",ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class) // Custom exception
    public ResponseEntity<SimpleErrorResponse> handleValidation(BadRequestException ex) {
        SimpleErrorResponse errorResponse = new SimpleErrorResponse(
                ex.getMessage(),
                String.valueOf(HttpStatus.BAD_REQUEST.value())
        );

        LOGGER.error("Resource validation exception:{}",ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidIdCsvException.class) // InvalidIdCsvException
    public ResponseEntity<SimpleErrorResponse> handleIdCsv(InvalidIdCsvException ex) {
        SimpleErrorResponse errorResponse = new SimpleErrorResponse(
                ex.getMessage(),
                String.valueOf(HttpStatus.BAD_REQUEST.value())
        );

        LOGGER.error("InvalidIdCsv data:{}",ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SongMetadataAlreadyExistException.class) // Custom exception
    public ResponseEntity<SimpleErrorResponse> handleSongMetadataAlreadyExist(SongMetadataAlreadyExistException ex) {
        SimpleErrorResponse errorResponse = new SimpleErrorResponse(
                ex.getMessage(),
                String.valueOf(HttpStatus.CONFLICT.value())
        );

        LOGGER.error("SongMetadata already exists:{}",ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidations(ValidationException ex) {
        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                "Validation error",
                ex.getDetails(),
                String.valueOf(HttpStatus.BAD_REQUEST.value())
        );
        LOGGER.error("Validation error:{}",ex.getDetails());
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
        LOGGER.error("Validation error:{}",details);
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
        LOGGER.error("Unknown error",ex);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}