package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.exception.types.DateTimeException;
import ru.practicum.exception.types.IntegrityViolationException;
import ru.practicum.exception.types.NotFoundException;
import ru.practicum.exception.types.RestrictionsViolationException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleCommonException(Exception e) {
        log.error("Ошибка 500: {}", e.getMessage());
        return buildApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера", e);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.error("Ошибка 400: Отсутствует обязательный параметр запроса - {}", e.getParameterName());
        return buildApiError(HttpStatus.BAD_REQUEST, "Неверно сформирован запрос", e);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleDateTimeException(DateTimeException e) {
        log.error("Ошибка 400: Некорректный запрос с датой и временем - {}", e.getMessage());
        return buildApiError(HttpStatus.BAD_REQUEST, "Неверно сформирован запрос с датой и временем", e);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException e) {
        log.error("Ошибка 404: Объект не найден - {}", e.getMessage());
        return buildApiError(HttpStatus.NOT_FOUND, "Запрашиваемый объект не найден", e);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleIntegrityViolationException(IntegrityViolationException e) {
        log.error("Ошибка 409: Нарушение целостности данных - {}", e.getMessage());
        return buildApiError(HttpStatus.CONFLICT, "Нарушение ограничения целостности", e);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleRestrictionsViolationException(RestrictionsViolationException e) {
        log.error("Ошибка 409: Не выполнены условия для выполнения операции - {}", e.getMessage());
        return buildApiError(HttpStatus.CONFLICT, "Для выполнения запроса условия не соблюдены", e);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("Ошибка 400: Некорректные аргументы запроса - {}", e.getMessage());
        String violations = e.getBindingResult().getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return buildApiError(HttpStatus.BAD_REQUEST, "Неверно сформирован запрос", violations, e);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.error("Ошибка 400: Несовпадение типа аргумента - {}", e.getMessage());
        return buildApiError(HttpStatus.BAD_REQUEST, "Неверно сформирован запрос", e);
    }

    private ApiError buildApiError(HttpStatus status, String reason, Exception e) {
        return ApiError.builder()
                .status(status.name())
                .reason(reason)
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .errors(ExceptionUtils.getStackTrace(e))
                .build();
    }

    private ApiError buildApiError(HttpStatus status, String reason, String message, Exception e) {
        return ApiError.builder()
                .status(status.name())
                .reason(reason)
                .message(message)
                .timestamp(LocalDateTime.now())
                .errors(ExceptionUtils.getStackTrace(e))
                .build();
    }
}
