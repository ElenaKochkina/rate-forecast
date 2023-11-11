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

    /**
     * Прогнозирование курса валюты на заданный период по алгоритму 'Мистический'.
     *
     * @param currencyData Исторические данные о курсе валюты.
     * @param startDate    Начальная дата прогноза.
     * @param endDate      Конечная дата прогноза.
     * @return Список прогнозируемых курсов валюты.
     */
    @Override
    public List<Currency> forecastCurrencyRates(List<Currency> currencyData, LocalDate startDate, LocalDate endDate) {
        log.info("Формирование прогноза курсов валют по алгоритму 'Мистический' на период {} - {}", startDate, endDate);
        List<Currency> forecastedRates = new ArrayList<>();
        LocalDate targetDate = startDate;
        while (!targetDate.isAfter(endDate)) {
            log.debug("Формирование прогноза курса валюты по алгоритму 'Мистический' на дату {}", targetDate);
            BigDecimal forecastedRate = forecastCurrencyRate(currencyData, targetDate);
            Currency forecastedCurrency = new Currency(targetDate, forecastedRate);
            forecastedRates.add(forecastedCurrency);
            log.debug("Сформирован прогноз курса валюты по алгоритму 'Мистический' на дату {}", targetDate);
            targetDate = targetDate.plusDays(1);
        }
        log.info("Сформирован прогноз курсов валют по алгоритму 'Мистический' на период {} - {}", startDate, endDate);
        return forecastedRates;
    }

    /**
     * Прогнозирует курс валюты на заданную дату по алгоритму 'Мистический'.
     *
     * @param currencyData Исторические данные о курсе валюты.
     * @param date         Дата, на которую выполняется прогноз.
     * @return Прогнозируемый курс валюты.
     * @throws PredictionDataException если данных недостаточно для прогноза на указанную дату.
     */
    private BigDecimal forecastCurrencyRate(List<Currency> currencyData, LocalDate date) {
        List<Currency> predictionData = currencyData.stream()
                .filter(currency -> isSameDayAndMonth(currency.getRateDate(), date))
                .collect(Collectors.toList());
        if (predictionData.isEmpty()) {
            log.error("Недостаточно данных для прогноза на дату: {}", date);
            throw new PredictionDataException(String.format("Недостаточно данных для прогноза на дату: %s", date));
        }
        int randomIndex = random.nextInt(predictionData.size());
        return predictionData.get(randomIndex).getRate();
    }

    /**
     * Проверяет, совпадают ли день и месяц у двух дат.
     *
     * @param date1 Первая дата.
     * @param date2 Вторая дата.
     * @return true, если день и месяц совпадают, иначе false.
     */
    private boolean isSameDayAndMonth(LocalDate date1, LocalDate date2) {
        return date1.getDayOfMonth() == date2.getDayOfMonth() && date1.getMonth() == date2.getMonth();
    }
}
