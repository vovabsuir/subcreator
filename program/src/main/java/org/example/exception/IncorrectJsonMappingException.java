package org.example.exception;

public class IncorrectJsonMappingException extends RuntimeException {
    public IncorrectJsonMappingException(String message) {
        super(message);
    }
}
