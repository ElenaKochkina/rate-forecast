package ru.liga.domain;

public enum ForecastType {
    TOMORROW("tomorrow"),
    WEEK("week");

    private final String value;

    ForecastType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ForecastType getByValue(String value) {
        for (ForecastType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Недопустимый тип прогноза: " + value);
    }
}
