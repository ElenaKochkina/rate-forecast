package ru.liga.algorithm;

import lombok.extern.log4j.Log4j2;
import ru.liga.algorithm.utils.LinearRegression;
import ru.liga.domain.Currency;
import ru.liga.exceptions.PredictionDataException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class InternetAlgorithm implements ForecastAlgorithm {
    private static final int DATA_COUNT_FOR_FORECAST = 30;

    /**
     * Прогнозирование курса валюты на заданный период по алгоритму 'Из интернета'.
     *
     * @param currencyData Исторические данные о курсе валюты.
     * @param startDate    Начальная дата прогноза.
     * @param endDate      Конечная дата прогноза.
     * @return Список прогнозируемых курсов валюты.
     * @throws PredictionDataException в случае, если недостаточно данных для прогноза.
     */
    @Override
    public List<Currency> forecastCurrencyRates(List<Currency> currencyData, LocalDate startDate, LocalDate endDate) {
        log.info("Формирование прогноза курсов валюты по алгоритму 'Из интернета' на период {} - {}", startDate, endDate);
        if (currencyData.size() < DATA_COUNT_FOR_FORECAST) {
            log.error("Недостаточно данных для прогноза на период {} - {}", startDate, endDate);
            throw new PredictionDataException(String.format("Недостаточно данных для прогноза на период %s - %s",
                    startDate, endDate));
        }
        List<Currency> predictionData = currencyData.stream()
                .limit(DATA_COUNT_FOR_FORECAST)
                .sorted(Comparator.comparing(Currency::getRateDate))
                .collect(Collectors.toList());

        double[] x = new double[DATA_COUNT_FOR_FORECAST];
        double[] y = new double[DATA_COUNT_FOR_FORECAST];
        for (int i = 0; i < DATA_COUNT_FOR_FORECAST; i++) {
            x[i] = i;
            y[i] = predictionData.get(i).getRate().doubleValue();
        }

        LinearRegression regression = new LinearRegression(x, y);

        List<Currency> forecastedRates = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            log.debug("Формирование прогноза курса валюты по алгоритму 'Из интернета' на дату {}", date);
            double xValue = ChronoUnit.DAYS.between(startDate, date);
            double forecastedRate = regression.predict(xValue);
            Currency forecastedCurrency = new Currency(date, BigDecimal.valueOf(forecastedRate));
            forecastedRates.add(forecastedCurrency);
            log.debug("Сформирован прогноз курса валюты по алгоритму 'Из интернета' на дату {}", date);
        }
        log.info("Сформирован прогноз курсов валюты по алгоритму 'Из интернета' на период {} - {}", startDate, endDate);
        return forecastedRates;
    }
}
