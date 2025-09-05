package com.introms.exception;


public class SongMetadataAlreadyExistException extends RuntimeException {
    public SongMetadataAlreadyExistException(String message) {
        super(message);
    }
}