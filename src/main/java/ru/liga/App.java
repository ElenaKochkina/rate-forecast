package ru.liga;

import org.jfree.data.category.DefaultCategoryDataset;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.liga.algorithm.AlgorithmFactory;
import ru.liga.controller.ConsoleCommandParser;
import ru.liga.controller.ConsoleController;
import ru.liga.output.ChartOutputGenerator;
import ru.liga.output.ListOutputGenerator;
import ru.liga.parser.CsvParser;
import ru.liga.parser.CsvReader;
import ru.liga.service.CurrencyRateForecastingService;
import ru.liga.service.CurrencyRateStorage;
import ru.liga.telegram.ReplyKeyboardGenerator;
import ru.liga.telegram.SendMessageGenerator;
import ru.liga.telegram.TelegramBot;
import ru.liga.telegram.UserBotState;

public class App {

    public static void main(String[] args) {
        CurrencyRateStorage currencyRateStorage = new CurrencyRateStorage(
                new CsvParser(
                        new CsvReader()));
        currencyRateStorage.addCurrencyData();

        TelegramBot bot = new TelegramBot(
                new CurrencyRateForecastingService(
                        currencyRateStorage,
                        new AlgorithmFactory()),
                new SendMessageGenerator(),
                new ReplyKeyboardGenerator(),
                new ListOutputGenerator(),
                new ChartOutputGenerator(
                        new DefaultCategoryDataset()),
                new UserBotState());
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

        ConsoleController consoleController = new ConsoleController(
                new ConsoleCommandParser(),
                new CurrencyRateForecastingService(
                        currencyRateStorage,
                        new AlgorithmFactory()),
                new ListOutputGenerator());
        consoleController.listen();
    }
}