package ru.practicum.shareit.exception;

public class BookingStateBadRequestException extends RuntimeException {
    public BookingStateBadRequestException(String state) {
        super("Unknown state: " + state);
    }
}
