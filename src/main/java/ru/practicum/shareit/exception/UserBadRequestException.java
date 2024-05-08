package ru.practicum.shareit.exception;

public class UserBadRequestException extends RuntimeException {
    public UserBadRequestException(String message) {
        super(message);
    }
}