package com.introms.exception;

public class ResourceSaveException extends RuntimeException {
    public ResourceSaveException(String message) {
        super(message);
    }

    public ResourceSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
