package ru.liga.algorithm;

import ru.liga.domain.Currency;

import java.time.LocalDate;
import java.util.List;

public interface ForecastAlgorithm {
    /**
     * Прогнозирование курса валюты на заданный период.
     *
     * @param currencyData Исторические данные о курсе валюты.
     * @param startDate    Начальная дата прогноза.
     * @param endDate      Конечная дата прогноза.
     * @return Список прогнозируемых курсов валюты.
     */
    List<Currency> forecastCurrencyRates(List<Currency> currencyData, LocalDate startDate, LocalDate endDate);
}
