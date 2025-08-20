package org.example.exception;

public class SendingRequestException extends RuntimeException {
    public SendingRequestException(String message) {
        super(message);
    }
}
