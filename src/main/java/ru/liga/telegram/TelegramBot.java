package ru.liga.telegram;

import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.liga.domain.Command;
import ru.liga.domain.Currency;
import ru.liga.enums.*;
import ru.liga.exceptions.PredictionDataException;
import ru.liga.output.ChartOutputGenerator;
import ru.liga.output.ListOutputGenerator;
import ru.liga.service.CurrencyRateForecastingService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
public class TelegramBot extends TelegramLongPollingBot {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final String botUsername = "rate-forecaster";
    private final String botToken = "6595631701:AAHHG4HphplFWiGVg3hlY0trpTAwkFRDrFs";
    private final CurrencyRateForecastingService forecastingService;
    private final SendMessageGenerator sendMessageGenerator;
    private final ReplyKeyboardGenerator replyKeyboardGenerator;
    private TelegramBotState currentState = TelegramBotState.WAITING_START;
    private Command command;
    private List<CurrencyCode> selectedCurrencies = new ArrayList<>();

    public TelegramBot(CurrencyRateForecastingService forecastingService) {
        this.forecastingService = forecastingService;
        this.sendMessageGenerator = new SendMessageGenerator();
        this.replyKeyboardGenerator = new ReplyKeyboardGenerator();
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();

            switch (messageText) {
                case Constants.START_COMMAND -> handleStartCommand(chatId);
                case Constants.HELP_COMMAND -> handleHelpCommand(chatId);
                case Constants.EXIT_COMMAND -> handleExitCommand(chatId);
                case Constants.RATE_COMMAND -> handleRateCommand(chatId);
                default -> handleMessage(update);
            }
        }
    }

    private void handleStartCommand(String chatId) {
        sendMessage(chatId, Constants.START_MESSAGE);
        currentState = TelegramBotState.WAITING_START;
    }

    private void handleHelpCommand(String chatId) {
        sendMessage(chatId, Constants.HELP_MESSAGE);
    }

    private void handleExitCommand(String chatId) {
        sendMessage(chatId, Constants.EXIT_MESSAGE);
        currentState = TelegramBotState.WAITING_START;
    }

    private void handleRateCommand(String chatId) {
        ReplyKeyboard keyboard = replyKeyboardGenerator.getCurrencyKeyboard();
        sendMessageWithReplyKeyboard(chatId, Constants.RATE_MESSAGE, keyboard);

        command = new Command();
        selectedCurrencies = new ArrayList<>();
        currentState = TelegramBotState.WAITING_CURRENCY_CODES;
    }

    private void handleMessage(Update update) {
        String messageText = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();

        switch (currentState) {
            case WAITING_CURRENCY_CODES -> handleCurrencyCodeSelection(chatId, messageText);
            case WAITING_ALGORITHM -> handleAlgorithmSelection(chatId, messageText);
            case WAITING_DATE_OR_PERIOD -> handleDateOrPeriodSelection(chatId, messageText);
            case WAITING_PERIOD -> handlePeriodSelection(chatId, messageText);
            case WAITING_DATE -> handleDateSelection(chatId, messageText);
            case WAITING_OUTPUT_TYPE -> handleOutputTypeSelection(chatId, messageText);
        }
    }

    private void handleCurrencyCodeSelection(String chatId, String messageText) {
        if (messageText.equals(Constants.FINISH_BUTTON)) {
            if (selectedCurrencies.isEmpty()) {
                sendMessage(chatId, Constants.NO_SELECTED_CURRENCY_ERROR_MESSAGE);
            } else {
                ReplyKeyboard keyboard = replyKeyboardGenerator.getAlgorithmKeyboard();
                sendMessageWithReplyKeyboard(chatId, Constants.SELECT_ALGORITHM_MESSAGE, keyboard);
                currentState = TelegramBotState.WAITING_ALGORITHM;

            }
        } else {
            try {
                CurrencyCode currencyCode = CurrencyCode.valueOf(messageText);
                if (!selectedCurrencies.contains(currencyCode)) {
                    selectedCurrencies.add(currencyCode);
                }
                sendMessage(chatId, Constants.CURRENCY_CONFIRM_MESSAGE + selectedCurrencies);
            } catch (IllegalArgumentException e) {
                sendMessage(chatId, Constants.NO_SUCH_CURRENCY_ERROR_MESSAGE);
            }
        }
    }

    private void handleAlgorithmSelection(String chatId, String messageText) {
        try {
            AlgorithmType algorithmType = AlgorithmType.valueOf(messageText);
            command.setAlgorithmType(algorithmType);
            ReplyKeyboard keyboard = replyKeyboardGenerator.getPeriodOrDateKeyboard();
            sendMessageWithReplyKeyboard(chatId, Constants.SELECT_DATE_OR_PERIOD_MESSAGE, keyboard);
            command.setCurrencyCodes(selectedCurrencies);
            currentState = TelegramBotState.WAITING_DATE_OR_PERIOD;
        } catch (IllegalArgumentException e) {
            sendMessage(chatId, Constants.NO_SUCH_ALGORITHM_ERROR_MESSAGE);
        }
    }

    private void handleDateOrPeriodSelection(String chatId, String messageText) {
        try {
            ForecastDuration forecastDuration = ForecastDuration.valueOf(messageText);
            switch (forecastDuration) {
                case DATE -> {
                    ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove(true);
                    sendMessageWithReplyKeyboard(chatId, Constants.SELECT_DATE_MESSAGE, replyKeyboardRemove);
                    currentState = TelegramBotState.WAITING_DATE;
                }
                case PERIOD -> {
                    ReplyKeyboard keyboard = replyKeyboardGenerator.getPeriodKeyboard();
                    sendMessageWithReplyKeyboard(chatId, Constants.SELECT_PERIOD_MESSAGE, keyboard);
                    currentState = TelegramBotState.WAITING_PERIOD;
                }
            }
        } catch (IllegalArgumentException e) {
            sendMessage(chatId, Constants.DATE_OR_PERIOD_ERROR_MESSAGE);
        }
    }

    private void handleDateSelection(String chatId, String messageText) {
        LocalDate date = null;
        if (messageText.equalsIgnoreCase("tomorrow")) {
            date = LocalDate.now().plusDays(1);
        } else {
            try {
                date = LocalDate.parse(messageText, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                log.error("Ошибка парсинга даты: " + messageText, e);
            }
        }

        if (date != null && date.isAfter(LocalDate.now())) {
            command.setStartDate(date);
            command.setEndDate(date);
            command.setOutputType(OutputType.LIST);
            sendCurrencyForecast(chatId);
        } else {
            sendMessage(chatId, Constants.SELECT_DATE_ERROR_MESSAGE);
        }
    }

    private void handlePeriodSelection(String chatId, String messageText) {
        try {
            ForecastPeriod forecastPeriod = ForecastPeriod.valueOf(messageText);
            switch (forecastPeriod) {
                case WEEK -> {
                    command.setStartDate(LocalDate.now().plusDays(1));
                    command.setEndDate(LocalDate.now().plusDays(7));
                }
                case MONTH -> {
                    command.setStartDate(LocalDate.now().plusDays(1));
                    command.setEndDate(LocalDate.now().plusDays(30));
                }
            }
            ReplyKeyboard keyboard = replyKeyboardGenerator.getOutputTypeKeyboard();
            sendMessageWithReplyKeyboard(chatId, Constants.SELECT_OUTPUT_TYPE_MESSAGE, keyboard);
            currentState = TelegramBotState.WAITING_OUTPUT_TYPE;
        } catch (IllegalArgumentException e) {
            sendMessage(chatId, Constants.NO_SUCH_PERIOD_ERROR_MESSAGE);
        }
    }

    private void handleOutputTypeSelection(String chatId, String messageText) {
        try {
            OutputType outputType = OutputType.valueOf(messageText);
            command.setOutputType(outputType);
            sendCurrencyForecast(chatId);
        } catch (IllegalArgumentException e) {
            sendMessage(chatId, Constants.NO_SUCH_OUTPUT_TYPE_ERROR_MESSAGE);
        }
    }

    private void sendCurrencyForecast(String chatId) {
        try {
            Map<String, List<Currency>> forecastedCurrency = forecastingService.calculateCurrencyRates(command);

            switch (command.getOutputType()) {
                case LIST -> {
                    ListOutputGenerator listOutputGenerator = new ListOutputGenerator();
                    String currencies = listOutputGenerator.createList(forecastedCurrency);
                    sendMessage(chatId, currencies);
                }
                case GRAPH -> {
                    ChartOutputGenerator chartOutputGenerator = new ChartOutputGenerator();
                    byte[] chart = chartOutputGenerator.createChart(forecastedCurrency);
                    SendPhoto message = sendMessageGenerator.createPhotoMessage(chatId, chart);
                    executeMessage(message);
                }
            }
            ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove(true);
            sendMessageWithReplyKeyboard(chatId, Constants.FINISH_MESSAGE, replyKeyboardRemove);
            currentState = TelegramBotState.WAITING_START;
        } catch (PredictionDataException e) {
            ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove(true);
            sendMessageWithReplyKeyboard(chatId, Constants.NO_PREDICTION_DATA_ERROR_MESSAGE, replyKeyboardRemove);
            currentState = TelegramBotState.WAITING_START;
        }
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = sendMessageGenerator.createTextMessage(chatId, text);
        executeMessage(message);
    }

    private void sendMessageWithReplyKeyboard(String chatId, String text, ReplyKeyboard keyboard) {
        SendMessage message = sendMessageGenerator.createTextMessageWithReplyKeyboard(chatId, text, keyboard);
        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Telegram exception:", e);
            throw new RuntimeException("An exception has been occurred while sending response message");
        }
    }

    private void executeMessage(SendPhoto message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Telegram exception:", e);
            throw new RuntimeException("An exception has been occurred while sending response message");
        }
    }
}
