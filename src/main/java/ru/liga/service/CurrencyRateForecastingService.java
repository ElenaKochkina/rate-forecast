package ru.liga.service;

import lombok.RequiredArgsConstructor;
import ru.liga.algorithm.AverageRateCalculator;
import ru.liga.domain.Command;
import ru.liga.domain.Currency;
import ru.liga.domain.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CurrencyRateForecastingService {
    private static final int DAYS_FOR_TOMORROW_FORECAST = 1;
    private static final int DAYS_FOR_WEEK_FORECAST = 7;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("E dd.MM.yyyy");
    private final CurrencyRateStorage currencyRateStorage;

    public void calculateAndPrintCurrencyForecast(Command command) {
        List<Currency> currencyDataForForecasting = getCurrencyDataForForecasting(command.getCurrencyCode());
        List<Currency> forecastedCurrency;
        switch (command.getForecastType()) {
            case TOMORROW ->
                    forecastedCurrency = calculateCurrencyRates(currencyDataForForecasting, DAYS_FOR_TOMORROW_FORECAST);
            case WEEK ->
                    forecastedCurrency = calculateCurrencyRates(currencyDataForForecasting, DAYS_FOR_WEEK_FORECAST);
            default ->
                    throw new IllegalArgumentException("Неподдерживаемый тип прогноза: " + command.getForecastType());
        }
        forecastedCurrency.forEach(currency -> printCurrencyRate(currency));
    }

    private List<Currency> getCurrencyDataForForecasting(CurrencyCode currencyCode) {
        return currencyRateStorage.getCurrencyData(currencyCode);
    }

    private List<Currency> calculateCurrencyRates(List<Currency> currencyDataForForecasting, int forecastDays) {
        List<Currency> calculatedRates = new ArrayList<>();
        List<Currency> workingDataForForecasting = new ArrayList<>(currencyDataForForecasting);
        for (int i = 0; i < forecastDays; i++) {
            BigDecimal averageRate = AverageRateCalculator.getAverageRate(workingDataForForecasting);
            workingDataForForecasting.add(0, new Currency(LocalDate.now().plusDays(i + 1), averageRate));
            calculatedRates.add(workingDataForForecasting.get(0));
        }
        return calculatedRates;
    }

    public void printCurrencyRate(Currency currency) {
        String formattedDate = currency.getRateDate().format(DATE_FORMATTER);
        System.out.printf("%s - %.2f;%n", formattedDate, currency.getRate());
    }
}
