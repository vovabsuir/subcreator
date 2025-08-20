package org.example.exception;

public class WritingFileException extends RuntimeException {
    public WritingFileException(String message) {
        super(message);
    }
}
