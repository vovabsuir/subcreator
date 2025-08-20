package org.example.exception;

public class TooManyReequestsException extends RuntimeException {
    public TooManyReequestsException(String message) {
        super(message);
    }
}
