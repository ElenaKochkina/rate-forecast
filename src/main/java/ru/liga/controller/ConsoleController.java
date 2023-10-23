package ru.liga.controller;

import lombok.RequiredArgsConstructor;
import ru.liga.domain.Command;
import ru.liga.domain.CurrencyCode;
import ru.liga.domain.ForecastType;
import ru.liga.service.CurrencyRateForecastingService;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class ConsoleController {
    private final CurrencyRateForecastingService currencyRatePredictorService;
    private final Pattern commandPattern = Pattern.compile("^rate\\s(\\w+)\\s(\\w+)$");

    public void listen() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите команду:");

        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();

            if (input.equals("exit")) {
                System.exit(0);
            }

            Command command = parseInputCommand(input);
            if (command == null) {
                System.out.printf("Неправильный формат команды. Команда %s не может быть выполнена.%n", input);
            } else {
                currencyRatePredictorService.calculateAndPrintCurrencyForecast(command);
            }
        }
    }

    private Command parseInputCommand(String input) {
        Matcher matcher = commandPattern.matcher(input);
        if (matcher.matches()) {
            try {
                CurrencyCode currencyCode = CurrencyCode.customValueOf(matcher.group(1));
                ForecastType forecastType = ForecastType.getByValue(matcher.group(2));
                return new Command(currencyCode, forecastType);
            } catch (IllegalArgumentException e) {
                System.out.printf("Ошибка: %s%n", e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }
}