package ru.liga;

import ru.liga.controller.ConsoleController;
import ru.liga.parser.CsvParser;
import ru.liga.parser.CsvReader;
import ru.liga.service.CurrencyRateForecastingService;

public class App {
    public static void main(String[] args) {
        ConsoleController consoleController = new ConsoleController(
                new CurrencyRateForecastingService(
                        new CsvParser(new CsvReader())));
        consoleController.listen();
    }
}
