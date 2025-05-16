package ru.yandex.practicum.exceptions;

public class TimeConflictException extends RuntimeException {
    public TimeConflictException(String message) {
        super(message);
    }
}
