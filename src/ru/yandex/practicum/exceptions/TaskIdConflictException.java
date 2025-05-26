package ru.yandex.practicum.exceptions;

public class TaskIdConflictException extends RuntimeException {
    public TaskIdConflictException(String message) {
        super(message);
    }
}
