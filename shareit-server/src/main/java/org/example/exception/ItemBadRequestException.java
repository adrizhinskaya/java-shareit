package org.example.exception;

public class ItemBadRequestException extends RuntimeException {
    public ItemBadRequestException(String message) {
        super(message);
    }
}
