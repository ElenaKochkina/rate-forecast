package ru.liga.algorithm;

import ru.liga.domain.Currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class AverageRateCalculator {
    private static final int FORECAST_COURSE_COUNT = 7;

    private AverageRateCalculator() {
    }

    public static BigDecimal getAverageRate(List<Currency> currencyData) {
        if (currencyData.size() < FORECAST_COURSE_COUNT) {
            throw new PredictionDataException("Недостаточно данных для прогноза.");
        }
        List<Currency> currencyDataForForecasting = currencyData.subList(0, FORECAST_COURSE_COUNT);
        return currencyDataForForecasting.stream()
                .map(Currency::getRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(currencyDataForForecasting.size()), RoundingMode.HALF_UP);
    }
}
