package org.example.exception;

public class BookingStateBadRequestException extends RuntimeException {
    public BookingStateBadRequestException(String state) {
        super("Unknown state: " + state);
    }
}
