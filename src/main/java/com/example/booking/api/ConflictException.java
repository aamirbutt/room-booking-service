package com.example.booking.api;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
