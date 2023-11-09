package ru.liga.output;

import lombok.extern.log4j.Log4j2;
import ru.liga.domain.Currency;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Log4j2
public class ListOutputGenerator {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("E dd.MM.yyyy");
    public String createList(Map<String, List<Currency>> currencyData) {
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

        return String.valueOf(result);
    }
}
