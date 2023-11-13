package ru.liga.parser;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import ru.liga.domain.Currency;
import ru.liga.exceptions.CsvParserException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
public class CsvParser {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final CsvReader csvReader;

    /**
     * Производит парсинг данных о курсах валют из CSV файла.
     *
     * @param filePath Путь к CSV файлу с данными.
     * @return Список объектов Currency, представляющих данные о курсах валют.
     * @throws CsvParserException если произошла ошибка при парсинге данных.
     */
    public List<Currency> parseCurrencyDataFromFile(String filePath) {
        return csvReader.readAllLines(filePath).stream()
                .skip(1)
                .map(s -> s.split(";"))
                .map(fields -> new Currency(
                        parseDate(fields[1]),
                        parseRate(fields[2])))
                .collect(Collectors.toList());
    }

    /**
     * Производит парсинг строки с датой.
     *
     * @param dateStr Строка с датой.
     * @return Объект LocalDate, представляющий дату.
     * @throws CsvParserException если произошла ошибка при парсинге даты.
     */
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.error("Ошибка парсинга даты: {}. {}", dateStr, e.getMessage());
            throw new CsvParserException("Ошибка парсинга даты: " + dateStr, e);
        }
    }

    /**
     * Производит парсинг строки с курсом валюты.
     *
     * @param rateStr Строка с курсом валюты.
     * @return Объект BigDecimal, представляющий курс валюты.
     * @throws CsvParserException если произошла ошибка при парсинге курса валюты.
     */
    private BigDecimal parseRate(String rateStr) {
        try {
            return new BigDecimal(rateStr.replace(",", "."));
        } catch (NumberFormatException e) {
            log.error("Ошибка парсинга курса: {}. {}", rateStr, e.getMessage());
            throw new CsvParserException("Ошибка парсинга курса: " + rateStr, e);
        }
    }
}