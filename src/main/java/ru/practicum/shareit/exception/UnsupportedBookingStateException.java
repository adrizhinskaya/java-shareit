package ru.practicum.shareit.exception;

public class UnsupportedBookingStateException extends RuntimeException {
    public UnsupportedBookingStateException(String state) {
        super("Unknown state: " + state);
    }
}
