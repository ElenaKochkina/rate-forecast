package ru.liga.controller;

import lombok.RequiredArgsConstructor;
import ru.liga.domain.Command;
import ru.liga.domain.Currency;
import ru.liga.exceptions.PredictionDataException;
import ru.liga.output.ListOutputGenerator;
import ru.liga.service.CurrencyRateForecastingService;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

@RequiredArgsConstructor
public class ConsoleController {
    private final ConsoleCommandParser consoleCommandParser;
    private final CurrencyRateForecastingService forecastingService;

    /**
     * Слушает ввод пользователя из консоли и обрабатывает команды.
     */
    public void listen() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите команду:");

        while (scanner.hasNextLine()) {
            String input = scanner.nextLine();

            if (input.equals("exit")) {
                System.exit(0);
            } else if (input.equals("help")) {
                System.out.println("""
                        Для получения прогноза курса валюты введите команду в формате:
                        rate [Код валюты] [Период прогноза]
                        Например, rate TRY tomorrow
                        Для выхода из приложения введите команду exit""");
            } else {
                Command command = consoleCommandParser.parseInputCommand(input);
                if (command == null) {
                    System.out.printf("Неправильный формат команды. Команда %s не может быть выполнена%n", input);
                } else {
                    try {
                        Map<String, List<Currency>> forecastedCurrency = forecastingService.calculateCurrencyRates(command);
                        ListOutputGenerator listOutputGenerator = new ListOutputGenerator();
                        System.out.print(listOutputGenerator.createList(forecastedCurrency));
                    } catch (PredictionDataException e) {
                        System.out.print(e.getMessage());
                    }
                }
            }
        }
    }
}