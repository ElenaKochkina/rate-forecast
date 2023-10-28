package ru.liga;

import ru.liga.controller.ConsoleController;
import ru.liga.parser.CsvParser;
import ru.liga.parser.CsvReader;
import ru.liga.service.CurrencyRateForecastingService;
import ru.liga.service.CurrencyRateStorage;

public class App {
    public static void main(String[] args) {
        CurrencyRateStorage currencyRateStorage = new CurrencyRateStorage(new CsvParser(new CsvReader()));
        currencyRateStorage.addCurrencyData();

        ConsoleController consoleController = new ConsoleController(
                new CurrencyRateForecastingService(currencyRateStorage));
        consoleController.listen();
    }
}
