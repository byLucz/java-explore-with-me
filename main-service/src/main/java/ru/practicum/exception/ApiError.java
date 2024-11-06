package ru.practicum.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import ru.practicum.config.Formatter;

import java.time.LocalDateTime;

@Getter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiError {
    String status;
    String reason;
    String message;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Formatter.DATE_TIME_FORMAT)
    LocalDateTime timestamp;
    String errors;
}
