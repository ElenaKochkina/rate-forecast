package ru.liga.algorithm;

import lombok.extern.log4j.Log4j2;
import ru.liga.domain.Currency;
import ru.liga.exceptions.PredictionDataException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.math.RoundingMode.HALF_UP;

@Log4j2
public class MeanAlgorithm implements ForecastAlgorithm {
    private static final int DATA_COUNT_FOR_FORECAST = 7;

    /**
     * Прогнозирование курса валюты на заданный период по алгоритму 'Среднеарифметический'.
     *
     * @param currencyData Исторические данные о курсе валюты.
     * @param startDate    Начальная дата прогноза.
     * @param endDate      Конечная дата прогноза.
     * @return Список прогнозируемых курсов валюты.
     * @throws PredictionDataException в случае, если недостаточно данных для прогноза.
     */
    @Override
    public List<Currency> forecastCurrencyRates(List<Currency> currencyData, LocalDate startDate, LocalDate endDate) {
        log.info("Формирование прогноза курсов валют по алгоритму 'Среднеарифметический' на период {} - {}", startDate, endDate);
        if (currencyData.size() < DATA_COUNT_FOR_FORECAST) {
            log.error("Недостаточно данных для прогноза на период {} - {}", startDate, endDate);
            throw new PredictionDataException(String.format("Недостаточно данных для прогноза на период %s - %s",
                    startDate, endDate));
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
        log.info("Сформирован прогноз курсов валют по алгоритму 'Среднеарифметический' на период {} - {}", startDate, endDate);
        return forecastedRates.stream()
                .filter(currency -> currency.getRateDate().isAfter(startDate.minusDays(1)))
                .sorted(Comparator.comparing(Currency::getRateDate))
                .collect(Collectors.toList());
    }

    /**
     * Прогнозирует средний курс валюты на основе последних данных.
     *
     * @param currencyData Данные по курсу валюты.
     * @return Средний курс валюты.
     */
    private BigDecimal forecastCurrencyRate(List<Currency> currencyData) {
        return currencyData.stream()
                .limit(DATA_COUNT_FOR_FORECAST)
                .map(Currency::getRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(DATA_COUNT_FOR_FORECAST), HALF_UP);
    }
}