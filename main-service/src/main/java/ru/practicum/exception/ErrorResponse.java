package ru.practicum.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorResponse {
    private final HttpStatus status;
    private final String error;

    public ErrorResponse(HttpStatus status, String error) {
        this.status = status;
        this.error = error;
    }
}
