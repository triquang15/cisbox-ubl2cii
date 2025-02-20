package com.cisbox.exception;

public class FileValidationException extends RuntimeException {
    private static final long serialVersionUID = 718552902488119317L;

    public FileValidationException(String message) {
        super(message);
    }
}
