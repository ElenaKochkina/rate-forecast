package ru.liga.controller;

import lombok.RequiredArgsConstructor;
import ru.liga.domain.Command;
import ru.liga.domain.Currency;
import ru.liga.output.ListOutputGenerator;
import ru.liga.service.CurrencyRateForecastingService;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

@RequiredArgsConstructor
public class ConsoleController {
    private final ConsoleCommandParser consoleCommandParser;
    private final CurrencyRateForecastingService forecastingService;

    public void listen() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите команду:");

        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();

            if (input.equals("exit")) {
                System.exit(0);
            }

            if (input.equals("help")) {
                System.out.println("Для получения прогноза курса валюты введите команду в формате:\n" +
                        "rate [Код валюты] [Период прогноза]\n" +
                        "Например, rate TRY tomorrow");
            }

            Command command = consoleCommandParser.parseInputCommand(input);
            if (command == null) {
                System.out.printf("Неправильный формат команды. Команда %s не может быть выполнена\n", input);
            } else {
                Map<String, List<Currency>> forecastedCurrency = forecastingService.calculateCurrencyRates(command);
                ListOutputGenerator listOutputGenerator = new ListOutputGenerator();
                System.out.print(listOutputGenerator.createList(forecastedCurrency));
            }
        }
    }
}