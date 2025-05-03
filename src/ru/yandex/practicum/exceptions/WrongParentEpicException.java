package ru.yandex.practicum.exceptions;

public class WrongParentEpicException extends RuntimeException {
    public WrongParentEpicException(String message) {
        super(message);
    }
}
