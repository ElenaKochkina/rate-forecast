package ru.liga.algorithm;

public class PredictionDataException extends RuntimeException {
    public PredictionDataException(String message) {
        super(message);
    }

    public PredictionDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
