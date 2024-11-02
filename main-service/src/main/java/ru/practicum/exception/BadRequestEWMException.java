package ru.practicum.exception;

public class BadRequestEWMException extends RuntimeException {
    public BadRequestEWMException(final String message) {
        super(message);
    }
}
