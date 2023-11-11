package ru.liga.algorithm;

import lombok.extern.log4j.Log4j2;
import ru.liga.domain.Currency;
import ru.liga.exceptions.PredictionDataException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class LastYearAlgorithm implements ForecastAlgorithm {

    /**
     * Прогнозирование курса валюты на заданный период по алгоритму 'Прошлогодний'.
     *
     * @param currencyData Исторические данные о курсе валюты.
     * @param startDate    Начальная дата прогноза.
     * @param endDate      Конечная дата прогноза.
     * @return Список прогнозируемых курсов валюты.
     */
    @Override
    public List<Currency> forecastCurrencyRates(List<Currency> currencyData, LocalDate startDate, LocalDate endDate) {
        log.info("Формирование прогноза курсов валют по алгоритму 'Прошлогодний' на период {} - {}", startDate, endDate);
        List<Currency> forecastedRates = new ArrayList<>();
        LocalDate targetDate = startDate;
        while (!targetDate.isAfter(endDate)) {
            log.debug("Формирование прогноза курса валюты по алгоритму 'Прошлогодний' на дату {}", targetDate);
            BigDecimal forecastedRate = forecastCurrencyRate(currencyData, targetDate);
            Currency forecastedCurrency = new Currency(targetDate, forecastedRate);
            forecastedRates.add(forecastedCurrency);
            log.debug("Сформирован прогноз курса валюты по алгоритму 'Прошлогодний' на дату {}", targetDate);
            targetDate = targetDate.plusDays(1);
        }
        log.info("Сформирован прогноз курсов валют по алгоритму 'Прошлогодний' на период {} - {}", startDate, endDate);
        return forecastedRates;
    }

    /**
     * Прогнозирование курса валюты на заданную дату по алгоритму 'Прошлогодний'.
     *
     * @param currencyData Исторические данные о курсе валюты.
     * @param date         Дата, на которую выполняется прогноз.
     * @return Прогнозируемый курс валюты.
     * @throws PredictionDataException в случае, если недостаточно данных для прогноза.
     */
    private BigDecimal forecastCurrencyRate(List<Currency> currencyData, LocalDate date) {
        LocalDate lastAvailableDate = currencyData.get(0).getRateDate();
        LocalDate firstAvailableDate = currencyData.get(currencyData.size() - 1).getRateDate();
        LocalDate targetDate = date;
        while (targetDate.isAfter(lastAvailableDate)) {
            targetDate = targetDate.minusYears(1);
        }
        if (targetDate.isBefore(firstAvailableDate)) {
            log.error("Недостаточно данных для прогноза на дату {}", date);
            throw new PredictionDataException(String.format("Недостаточно данных для прогноза на дату: %s", date));
        }
        BigDecimal forecastedRate = null;
        while (forecastedRate == null) {
            forecastedRate = findRateForDate(currencyData, targetDate);
            targetDate = targetDate.minusDays(1);
        }
        return forecastedRate;
    }

    /**
     * Ищет курс валюты для заданной даты.
     *
     * @param currencyData Исторические данные о курсе валюты.
     * @param date         Дата, для которой ищется курс.
     * @return Курс валюты для заданной даты или {@code null}, если курс не найден.
     */
    private BigDecimal findRateForDate(List<Currency> currencyData, LocalDate date) {
        return currencyData.stream()
                .filter(currency -> currency.getRateDate().isEqual(date))
                .findFirst()
                .map(Currency::getRate)
                .orElse(null);
    }
}