package ru.liga.algorithm;

import ru.liga.domain.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ForecastAlgorithm {
    List<Currency> forecastCurrencyRates(List<Currency> currencyData, LocalDate startDate, LocalDate endDate);
}
