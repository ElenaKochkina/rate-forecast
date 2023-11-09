package ru.liga.algorithm;

import ru.liga.domain.Currency;
import ru.liga.exceptions.PredictionDataException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.math.RoundingMode.HALF_UP;

public class MeanAlgorithm implements ForecastAlgorithm {
    private static final int DATA_COUNT_FOR_FORECAST = 7;

    @Override
    public List<Currency> forecastCurrencyRates(List<Currency> currencyData, LocalDate startDate, LocalDate endDate) {
        if (currencyData.size() < DATA_COUNT_FOR_FORECAST) {
            throw new PredictionDataException(String.format("Недостаточно данных для прогноза на дату: %s", startDate));
        }
        List<Currency> forecastedRates = new ArrayList<>(currencyData);
        LocalDate lastAvailableDate = currencyData.get(0).getRateDate();
        LocalDate targetDate = lastAvailableDate.plusDays(1);
        while (!targetDate.isAfter(endDate)) {
            BigDecimal forecastedRate = forecastCurrencyRate(forecastedRates);
            Currency forecastedCurrency = new Currency(targetDate, forecastedRate);
            forecastedRates.add(0, forecastedCurrency);
            targetDate = targetDate.plusDays(1);
        }
        return forecastedRates.stream()
                .filter(currency -> currency.getRateDate().isAfter(startDate.minusDays(1)))
                .sorted(Comparator.comparing(Currency::getRateDate))
                .collect(Collectors.toList());
    }

    private BigDecimal forecastCurrencyRate(List<Currency> currencyData) {
        return currencyData.stream()
                .limit(DATA_COUNT_FOR_FORECAST)
                .map(Currency::getRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(DATA_COUNT_FOR_FORECAST), HALF_UP);
    }
}