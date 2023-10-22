package ru.liga.parser;

import lombok.RequiredArgsConstructor;
import ru.liga.domain.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CsvParser {
    private final CsvReader csvReader;

    public List<Currency> parseCurrencyDataFromFile(String filePath) {
        return csvReader.readAllLines(filePath).stream()
                .skip(1)
                .map(s -> s.split(";"))
                .map(fields -> new Currency(
                        parseDate(fields[1]),
                        parseRate(fields[2])))
                .collect(Collectors.toList());
    }

    private LocalDate parseDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        try {
            return LocalDate.parse(dateStr, formatter);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Ошибка парсинга даты: " + dateStr, e);
        }
    }

    private BigDecimal parseRate(String rateStr) {
        try {
            return new BigDecimal(rateStr.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Ошибка парсинга курса: " + rateStr, e);
        }
    }
}