package ru.liga.controller;

import lombok.extern.log4j.Log4j2;
import ru.liga.domain.Command;
import ru.liga.enums.AlgorithmType;
import ru.liga.enums.CurrencyCode;
import ru.liga.enums.ForecastRange;
import ru.liga.enums.OutputType;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class ConsoleCommandParser {
    private final Pattern commandPattern = Pattern.compile("^rate\\s(\\w+)\\s(\\w+)$");

    public Command parseInputCommand(String input) {
        Matcher matcher = commandPattern.matcher(input);
        if (matcher.matches()) {
            try {
                CurrencyCode currencyCode = CurrencyCode.customValueOf(matcher.group(1));
                ForecastRange forecastType = ForecastRange.getByValue(matcher.group(2));
                return switch (forecastType) {
                    case TOMORROW -> new Command(List.of(currencyCode),
                            LocalDate.now().plusDays(1),
                            LocalDate.now().plusDays(1),
                            AlgorithmType.MEAN,
                            OutputType.LIST);
                    case WEEK -> new Command(List.of(currencyCode),
                            LocalDate.now().plusDays(1),
                            LocalDate.now().plusDays(7),
                            AlgorithmType.MEAN,
                            OutputType.LIST);
                };
            } catch (IllegalArgumentException e) {
                log.warn("Ошибка при обработке введенной команды. {}", e.getMessage());
                return null;
            }
        } else {
            log.warn("Неправильный формат команды. Команда '{}' не может быть выполнена", input);
            return null;
        }
    }
}
