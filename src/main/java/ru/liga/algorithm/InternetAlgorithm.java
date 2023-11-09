package ru.liga.algorithm;

import ru.liga.algorithm.utils.LinearRegression;
import ru.liga.domain.Currency;
import ru.liga.exceptions.PredictionDataException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class InternetAlgorithm implements ForecastAlgorithm {
    private static final int DATA_COUNT_FOR_FORECAST = 30;

    @Override
    public List<Currency> forecastCurrencyRates(List<Currency> currencyData, LocalDate startDate, LocalDate endDate) {
        if (currencyData.size() < DATA_COUNT_FOR_FORECAST) {
            throw new PredictionDataException(String.format("Недостаточно данных для прогноза на дату: %s", startDate));
        }
        List<Currency> predictionData = currencyData.stream()
                .limit(DATA_COUNT_FOR_FORECAST)
                .sorted(Comparator.comparing(Currency::getRateDate))
                .collect(Collectors.toList());

        double[] x = new double[DATA_COUNT_FOR_FORECAST];
        double[] y = new double[DATA_COUNT_FOR_FORECAST];
        for (int i = 0; i < DATA_COUNT_FOR_FORECAST; i++) {
            x[i] = i;
            y[i] = predictionData.get(i).getRate().doubleValue();
        }

        LinearRegression regression = new LinearRegression(x, y);

        List<Currency> forecastedRates = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            double xValue = ChronoUnit.DAYS.between(startDate, date);
            double forecastedRate = regression.predict(xValue);
            Currency forecastedCurrency = new Currency(date, BigDecimal.valueOf(forecastedRate));
            forecastedRates.add(forecastedCurrency);
        }
        return forecastedRates;
    }
}
