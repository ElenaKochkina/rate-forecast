package ru.liga.output;

import lombok.extern.log4j.Log4j2;
import ru.liga.domain.Currency;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Log4j2
public class ListOutputGenerator {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("E dd.MM.yyyy");

    /**
     * Создает текстовое представление списка прогнозируемых курсов валют.
     *
     * @param currencyData Прогнозируемые курсы валют, сгруппированные по коду валюты.
     * @return Текстовое представление списка прогнозируемых курсов валют.
     */
    public String createList(Map<String, List<Currency>> currencyData) {
        log.info("Формирование списка прогнозируемых курсов валют для вывода пользователю");

        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, List<Currency>> entry : currencyData.entrySet()) {
            String currencyCode = entry.getKey();
            result.append(String.format("Прогноз курса %s%n", currencyCode));

            List<Currency> currencyList = entry.getValue();
            for (Currency currency : currencyList) {
                String formattedDate = currency.getRateDate().format(DATE_FORMATTER);
                result.append(String.format("%s - %.2f;%n", formattedDate, currency.getRate()));
            }
            result.append("\n");
        }
        log.info("Список прогнозируемых курсов валют для вывода пользователю успешно сформирован");
        return String.valueOf(result);
    }
}
