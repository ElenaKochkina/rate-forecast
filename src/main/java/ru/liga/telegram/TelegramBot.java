package ru.liga.telegram;

import lombok.RequiredArgsConstructor;
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
import ru.liga.enums.AlgorithmType;
import ru.liga.enums.CurrencyCode;
import ru.liga.enums.ForecastPeriod;
import ru.liga.enums.OutputType;
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
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final String botUsername = "rate-forecaster";
    private final String botToken = "6595631701:AAHHG4HphplFWiGVg3hlY0trpTAwkFRDrFs";
    private final SendMessageGenerator sendMessageGenerator = new SendMessageGenerator();
    private final ReplyKeyboarGenerator replyKeyboardGenerator = new ReplyKeyboarGenerator();
    private final CurrencyRateForecastingService forecastingService;
    private TelegramBotState currentState = TelegramBotState.WAITING_START;
    private Command command;
    private List<CurrencyCode> selectedCurrencies = new ArrayList<>();

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
            //todo enum
            switch (messageText) {
                case "/start" -> {
                    SendMessage sendMessage = sendMessageGenerator.createTextMessage(chatId, Messages.START_MESSAGE);
                    executeMessage(sendMessage);
                    currentState = TelegramBotState.WAITING_START;
                }
                case "/help" -> {
                    SendMessage sendMessage = sendMessageGenerator.createTextMessage(chatId, Messages.HELP_MESSAGE);
                    executeMessage(sendMessage);
                }
                case "/exit" -> {
                    SendMessage sendMessage = sendMessageGenerator.createTextMessage(chatId, Messages.EXIT_MESSAGE);
                    executeMessage(sendMessage);
                    currentState = TelegramBotState.WAITING_START;
                }
                case "/rate" -> {
                    ReplyKeyboard keyboard = replyKeyboardGenerator.getCurrencyKeyboard();
                    SendMessage sendMessage = sendMessageGenerator.createTextMessageWithReplyKeyboard(chatId,
                            Messages.RATE_MESSAGE, keyboard);
                    executeMessage(sendMessage);
                    command = new Command();
                    selectedCurrencies = new ArrayList<>();
                    currentState = TelegramBotState.WAITING_CURRENCY_CODES;
                }
                default -> handleMessage(update);
            }
        }
    }

    private void handleMessage(Update update) {
        String messageText = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();

        switch (currentState) {
            case WAITING_CURRENCY_CODES -> handleCurrencyCodeSelection(chatId, messageText);
            case WAITING_DATE_OR_PERIOD -> handleDateOrPeriodSelection(chatId, messageText);
            case WAITING_PERIOD -> handlePeriodSelection(chatId, messageText);
            case WAITING_DATE -> handleDateSelection(chatId, messageText);
            case WAITING_ALGORITHM -> handleAlgorithmSelection(chatId, messageText);
            case WAITING_OUTPUT_TYPE -> handleOutputTypeSelection(chatId, messageText);
        }
    }

    private void handleCurrencyCodeSelection(String chatId, String messageText) {
        if (messageText.equals("Завершить")) {
            if (selectedCurrencies.isEmpty()) {
                SendMessage message = sendMessageGenerator.createTextMessage(chatId,
                        Messages.NOT_SELECTED_CURRENCY_ERROR_MESSAGE);
                executeMessage(message);
            } else {
                ReplyKeyboard keyboard = replyKeyboardGenerator.getPeriodOrDateKeyboard();
                SendMessage message = sendMessageGenerator.createTextMessageWithReplyKeyboard(chatId,
                        Messages.SELECT_DATE_OR_PERIOD_MESSAGE, keyboard);
                executeMessage(message);
                command.setCurrencyCodes(selectedCurrencies);
                currentState = TelegramBotState.WAITING_DATE_OR_PERIOD;
            }
        } else {
            try {
                CurrencyCode currencyCode = CurrencyCode.valueOf(messageText);
                if (!selectedCurrencies.contains(currencyCode)) {
                    selectedCurrencies.add(currencyCode);
                }
                String confirmationMessage = Messages.CURRENCY_CONFIRM_MESSAGE + selectedCurrencies;
                SendMessage confirmation = sendMessageGenerator.createTextMessage(chatId, confirmationMessage);
                executeMessage(confirmation);
            } catch (IllegalArgumentException e) {
                SendMessage errorMessage = sendMessageGenerator.createTextMessage(chatId,
                        Messages.NO_SUCH_CURRENCY_ERROR_MESSAGE);
                executeMessage(errorMessage);
            }
        }
    }

    private void handleDateOrPeriodSelection(String chatId, String messageText) {
        switch (messageText) {
            //todo enam
            case "Date" -> {
                ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove(true);
                SendMessage message = sendMessageGenerator.createTextMessageWithReplyKeyboard(chatId,
                        Messages.SELECT_DATE_MESSAGE, replyKeyboardRemove);
                executeMessage(message);
                currentState = TelegramBotState.WAITING_DATE;
            }
            case "Period" -> {
                ReplyKeyboard keyboard = replyKeyboardGenerator.getPeriodKeyboard();
                SendMessage message = sendMessageGenerator.createTextMessageWithReplyKeyboard(chatId,
                        Messages.SELECT_PERIOD_MESSAGE, keyboard);
                executeMessage(message);
                currentState = TelegramBotState.WAITING_PERIOD;
            }
            default -> {
                SendMessage errorMessage = sendMessageGenerator.createTextMessage(chatId,
                        Messages.DATE_OR_PERIOD_ERROR_MESSAGE);
                executeMessage(errorMessage);
            }
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

            ReplyKeyboard keyboard = replyKeyboardGenerator.getAlgorithmKeyboard();
            SendMessage message = sendMessageGenerator.createTextMessageWithReplyKeyboard(chatId,
                    Messages.SELECT_ALGORITHM_MESSAGE, keyboard);
            executeMessage(message);
            currentState = TelegramBotState.WAITING_ALGORITHM;
        } else {
            SendMessage errorMessage = sendMessageGenerator.createTextMessage(chatId,
                    Messages.SELECT_DATE_ERROR_MESSAGE);
            executeMessage(errorMessage);
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
            ReplyKeyboard keyboard = replyKeyboardGenerator.getAlgorithmKeyboard();
            SendMessage message = sendMessageGenerator.createTextMessageWithReplyKeyboard(chatId,
                    Messages.SELECT_ALGORITHM_MESSAGE, keyboard);
            executeMessage(message);
            currentState = TelegramBotState.WAITING_ALGORITHM;
        } catch (IllegalArgumentException e) {
            SendMessage errorMessage = sendMessageGenerator.createTextMessage(chatId,
                    Messages.NO_SUCH_PERIOD_ERROR_MESSAGE);
            executeMessage(errorMessage);
        }
    }

    private void handleAlgorithmSelection(String chatId, String messageText) {
        try {
            AlgorithmType algorithmType = AlgorithmType.valueOf(messageText);
            command.setAlgorithmType(algorithmType);
            ReplyKeyboard keyboard = replyKeyboardGenerator.getOutputTypeKeyboard();
            SendMessage message = sendMessageGenerator.createTextMessageWithReplyKeyboard(chatId,
                    Messages.SELECT_OUTPUT_TYPE_MESSAGE, keyboard);
            executeMessage(message);
            currentState = TelegramBotState.WAITING_OUTPUT_TYPE;
        } catch (IllegalArgumentException e) {
            SendMessage errorMessage = sendMessageGenerator.createTextMessage(chatId,
                    Messages.NO_SUCH_ALGORITHM_ERROR_MESSAGE);
            executeMessage(errorMessage);
        }
    }

    private void handleOutputTypeSelection(String chatId, String messageText) {
        try {
            OutputType outputType = OutputType.valueOf(messageText);
            command.setOutputType(outputType);

            Map<String, List<Currency>> forecastedCurrency = forecastingService.calculateCurrencyRates(command);

            switch (command.getOutputType()) {
                case LIST -> {
                    ListOutputGenerator listOutputGenerator = new ListOutputGenerator();
                    String currencies = listOutputGenerator.createList(forecastedCurrency);
                    SendMessage message = sendMessageGenerator.createTextMessage(chatId, currencies);
                    executeMessage(message);
                }
                case GRAPH -> {
                    ChartOutputGenerator chartOutputGenerator = new ChartOutputGenerator();
                    byte[] chart = chartOutputGenerator.createChart(forecastedCurrency);
                    SendPhoto message = sendMessageGenerator.createPhotoMessage(chatId, chart);
                    executeMessage(message);
                }
            }
            ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove(true);
            SendMessage message = sendMessageGenerator.createTextMessageWithReplyKeyboard(chatId,
                    Messages.FINISH_MESSAGE, replyKeyboardRemove);
            executeMessage(message);
            currentState = TelegramBotState.WAITING_START;
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            SendMessage errorMessage = sendMessageGenerator.createTextMessage(chatId,
                    Messages.NO_SUCH_OUTPUT_TYPE_ERROR_MESSAGE);
            executeMessage(errorMessage);
        } catch (PredictionDataException e) {
            //todo дописать на возвращение к дате
            SendMessage errorMessage = sendMessageGenerator.createTextMessage(chatId,
                    "Написать сообщение об ошибке");
            executeMessage(errorMessage);
        }
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
