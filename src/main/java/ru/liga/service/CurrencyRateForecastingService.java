package ru.liga.service;

import ru.liga.algorithm.AverageRateCalculator;
import ru.liga.domain.Command;
import ru.liga.domain.Currency;
import ru.liga.domain.CurrencyCode;
import ru.liga.parser.CsvParser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CurrencyRateForecastingService {
    CsvParser csvParser;
    private static final int DAYS_IN_WEEK = 7;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("E dd.MM.yyyy");

    public CurrencyRateForecastingService(CsvParser csvParser) {
        this.csvParser = csvParser;
    }

    public void calculateAndPrintCurrencyForecast(Command command) {
        List<Currency> currencyDataForForecasting = getCurrencyDataForForecasting(command.getCurrencyCode());
        switch (command.getForecastType()) {
            case TOMORROW -> {
                Currency currency = calculateCurrencyRateForTomorrow(currencyDataForForecasting);
                printCurrencyRate(currency);
            }
            case WEEK -> {
                List<Currency> currencyList = calculateCurrencyRatesForWeek(currencyDataForForecasting);
                currencyList.forEach(currency -> printCurrencyRate(currency));
            }
            default ->
                    throw new IllegalArgumentException("Неподдерживаемый тип прогноза: " + command.getForecastType());
        }
    }

    private List<Currency> getCurrencyDataForForecasting(CurrencyCode currencyCode) {
        return csvParser.parseCurrencyDataFromFile(getFileName(currencyCode));
    }

    private String getFileName(CurrencyCode currencyCode) {
        return currencyCode.name().toLowerCase() + ".csv";
    }

    private Currency calculateCurrencyRateForTomorrow(List<Currency> currencyDataForForecasting) {
        BigDecimal rate = AverageRateCalculator.getAverageRate(currencyDataForForecasting);
        return new Currency(LocalDate.now().plusDays(1), rate);
    }

    private List<Currency> calculateCurrencyRatesForWeek(List<Currency> currencyDataForForecasting) {
        List<Currency> calculatedRates = new ArrayList<>();
        for (int i = 0; i < DAYS_IN_WEEK; i++) {
            BigDecimal averageRate = AverageRateCalculator.getAverageRate(currencyDataForForecasting);
            currencyDataForForecasting.add(0, new Currency(LocalDate.now().plusDays(i + 1), averageRate));
            calculatedRates.add(currencyDataForForecasting.get(0));
        }
        return calculatedRates;
    }

    public void printCurrencyRate(Currency currency) {
        String formattedDate = currency.getRateDate().format(DATE_FORMATTER);
        System.out.printf("%s - %.2f;%n", formattedDate, currency.getRate());
    }
}
