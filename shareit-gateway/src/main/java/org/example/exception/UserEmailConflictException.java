package org.example.exception;

public class UserEmailConflictException extends RuntimeException {
    public UserEmailConflictException(String message) {
        super(message);
    }
}