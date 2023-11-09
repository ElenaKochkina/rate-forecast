package ru.liga.enums;

public enum ForecastRange {
    TOMORROW("tomorrow"),
    WEEK("week");

    private final String value;

    ForecastRange(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ForecastRange getByValue(String value) {
        for (ForecastRange type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Недопустимый период прогноза: " + value);
    }
}
