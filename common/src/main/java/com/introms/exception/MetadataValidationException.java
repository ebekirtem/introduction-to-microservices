package com.introms.exception;


import java.util.Map;

public class MetadataValidationException extends RuntimeException {
    private final Map<String,String> details;

    public MetadataValidationException(String message, Map<String,String> details) {
        super(message);
        this.details=details;
    }

    public Map<String,String> getDetails(){
        return this.details;
    }
}