package ru.liga.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import ru.liga.algorithm.ForecastAlgorithm;
import ru.liga.domain.Command;
import ru.liga.domain.Currency;
import ru.liga.enums.CurrencyCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
public class CurrencyRateForecastingService {
    private final CurrencyRateStorage currencyRateStorage;

    /**
     * Вычисляет прогнозируемые курсы валют на основе переданных параметров команды.
     *
     * @param command Объект, содержащий параметры для прогнозирования.
     * @return Прогнозируемые курсы валют, сгруппированные по коду валюты.
     */
    public Map<String, List<Currency>> calculateCurrencyRates(Command command) {
        log.info("Начало расчета прогноза курсов валют. {}", command);
        AlgorithmFactory algorithmFactory = new AlgorithmFactory();
        ForecastAlgorithm forecastAlgorithm = algorithmFactory.createAlgorithm(command.getAlgorithmType());

        Map<String, List<Currency>> forecastedCurrencyRates = new HashMap<>();
        command.getCurrencyCodes().forEach(currencyCode -> {
            List<Currency> currencyDataForForecasting = getCurrencyDataForForecasting(currencyCode);
            List<Currency> calculatedRates = forecastAlgorithm.forecastCurrencyRates(currencyDataForForecasting,
                    command.getStartDate(), command.getEndDate());
            forecastedCurrencyRates.put(String.valueOf(currencyCode), calculatedRates);
        });
        log.info("Расчет прогноза курсов валют завершен");
        return forecastedCurrencyRates;
    }

    /**
     * Получает исторические данные о курсах валют для заданного кода валюты.
     *
     * @param currencyCode Код валюты.
     * @return Исторические данные о курсах валюты.
     */
    private List<Currency> getCurrencyDataForForecasting(CurrencyCode currencyCode) {
        return currencyRateStorage.getCurrencyDataByCurrencyCode(currencyCode);
    }
}
