package ru.liga.exceptions;

public class NoSuchAlgorithmException extends RuntimeException {
    public NoSuchAlgorithmException(String message) {
        super(message);
    }

    public NoSuchAlgorithmException(String message, Throwable cause) {
        super(message, cause);
    }
}
