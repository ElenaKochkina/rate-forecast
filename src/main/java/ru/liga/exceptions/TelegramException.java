package ru.liga.exceptions;

public class TelegramException extends RuntimeException {
    public TelegramException(String message) {
        super(message);
    }
}
