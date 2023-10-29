package ru.liga.domain;

public enum CurrencyCode {
    EUR,
    USD,
    TRY,
    BGN,
    AMD;

    public static CurrencyCode customValueOf(String value) {
        for (CurrencyCode code : values()) {
            if (code.name().equalsIgnoreCase(value)) {
                return code;
            }
        }
        throw new IllegalArgumentException("Недопустимый тип валюты: " + value);
    }
}
