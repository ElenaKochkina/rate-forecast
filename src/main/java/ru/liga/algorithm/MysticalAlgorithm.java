package ru.liga.algorithm;
import lombok.extern.log4j.Log4j2;
import ru.liga.domain.Currency;
import ru.liga.exceptions.PredictionDataException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Log4j2
public class MysticalAlgorithm implements ForecastAlgorithm {
    private final Random random = new Random();

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
        List<Currency> predictionData = currencyData.stream()
                .filter(currency -> isSameDayAndMonth(currency.getRateDate(), date))
                .collect(Collectors.toList());
        if (predictionData.isEmpty()) {
            log.error(String.format("Недостаточно данных для прогноза на дату: %s", date));
            throw new PredictionDataException(String.format("Недостаточно данных для прогноза на дату: %s", date));
        }
        int randomIndex = random.nextInt(predictionData.size());
        return predictionData.get(randomIndex).getRate();
    }

    private boolean isSameDayAndMonth(LocalDate date1, LocalDate date2) {
        return date1.getDayOfMonth() == date2.getDayOfMonth() && date1.getMonth() == date2.getMonth();
    }
}
