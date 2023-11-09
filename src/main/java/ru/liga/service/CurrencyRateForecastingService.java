package ru.liga.service;

import lombok.RequiredArgsConstructor;
import ru.liga.algorithm.ForecastAlgorithm;
import ru.liga.domain.Command;
import ru.liga.domain.Currency;
import ru.liga.enums.CurrencyCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CurrencyRateForecastingService {
    private final CurrencyRateStorage currencyRateStorage;

    public Map<String, List<Currency>> calculateCurrencyRates(Command command) {
        AlgorithmFactory algorithmFactory = new AlgorithmFactory();
        ForecastAlgorithm forecastAlgorithm = algorithmFactory.createAlgorithm(command.getAlgorithmType());

        Map<String, List<Currency>> forecastedCurrencyRates = new HashMap<>();
        command.getCurrencyCodes().forEach(currencyCode -> {
            List<Currency> currencyDataForForecasting = getCurrencyDataForForecasting(currencyCode);
            List<Currency> calculatedRates = forecastAlgorithm.forecastCurrencyRates(currencyDataForForecasting,
                    command.getStartDate(), command.getEndDate());
            forecastedCurrencyRates.put(String.valueOf(currencyCode), calculatedRates);
        });
        return forecastedCurrencyRates;
    }

    private List<Currency> getCurrencyDataForForecasting(CurrencyCode currencyCode) {
        return currencyRateStorage.getCurrencyData(currencyCode);
    }
}
