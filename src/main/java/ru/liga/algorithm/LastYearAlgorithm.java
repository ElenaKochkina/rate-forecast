package ru.liga.algorithm;

import ru.liga.domain.Currency;
import ru.liga.exceptions.PredictionDataException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LastYearAlgorithm implements ForecastAlgorithm {

    @Override
    public List<Currency> forecastCurrencyRates(List<Currency> currencyData, LocalDate startDate, LocalDate endDate) {
        List<Currency> forecastedRates = new ArrayList<>();
        LocalDate targetDate = startDate;
        while (!targetDate.isAfter(endDate)) {
            BigDecimal forecastedRate = forecastCurrencyRate(currencyData, targetDate);
            Currency forecastedCurrency = new Currency(targetDate, forecastedRate);
            forecastedRates.add(forecastedCurrency);

            targetDate = targetDate.plusDays(1);
        }
        return forecastedRates;
    }

    private BigDecimal forecastCurrencyRate(List<Currency> currencyData, LocalDate date) {
        LocalDate lastAvailableDate = currencyData.get(0).getRateDate();
        LocalDate firstAvailableDate = currencyData.get(currencyData.size() - 1).getRateDate();
        LocalDate targetDate = date;
        while (targetDate.isAfter(lastAvailableDate)) {
            targetDate = targetDate.minusYears(1);
        }
        if (targetDate.isBefore(firstAvailableDate)){
            throw new PredictionDataException(String.format("Недостаточно данных для прогноза на дату: %s", date));
        }
        BigDecimal forecastedRate = null;
        while (forecastedRate == null){
            forecastedRate = findRateForDate(currencyData, targetDate);
            targetDate = targetDate.minusDays(1);
        }
        return forecastedRate;
    }

    private BigDecimal findRateForDate(List<Currency> currencyData, LocalDate date) {
        return currencyData.stream()
                .filter(currency -> currency.getRateDate().isEqual(date))
                .findFirst()
                .map(Currency::getRate)
                .orElse(null);
    }
}