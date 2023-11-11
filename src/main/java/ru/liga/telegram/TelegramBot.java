package ru.liga.telegram;

import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.liga.domain.Command;
import ru.liga.domain.Currency;
import ru.liga.enums.*;
import ru.liga.exceptions.PredictionDataException;
import ru.liga.exceptions.TelegramException;
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
                case Constants.START_COMMAND -> handleStartCommand(chatId, update.getMessage());
                case Constants.HELP_COMMAND -> handleHelpCommand(chatId, update.getMessage());
                case Constants.EXIT_COMMAND -> handleExitCommand(chatId, update.getMessage());
                case Constants.RATE_COMMAND -> handleRateCommand(chatId, update.getMessage());
                default -> handleMessage(update);
            }
        }
    }

    private void handleStartCommand(String chatId, Message msg) {
        log.info("Пользователь {}. Начато выполнение команды {}", getUserName(msg), Constants.START_COMMAND);
        sendMessage(chatId, Constants.START_MESSAGE);
        log.info("Пользователь {}. Отправлено сообщение: {}", getUserName(msg), Constants.START_MESSAGE);
        log.info("Пользователь {}. Завершено выполнение команды {}", getUserName(msg), Constants.START_COMMAND);
        currentState = TelegramBotState.WAITING_START;
    }

    private void handleHelpCommand(String chatId, Message msg) {
        log.info("Пользователь {}. Начато выполнение команды {}", getUserName(msg), Constants.HELP_COMMAND);
        sendMessage(chatId, Constants.HELP_MESSAGE);
        log.info("Пользователь {}. Отправлено сообщение: {}", getUserName(msg), Constants.HELP_MESSAGE);
        log.info("Пользователь {}. Завершено выполнение команды {}", getUserName(msg), Constants.HELP_COMMAND);
    }

    private void handleExitCommand(String chatId, Message msg) {
        log.info("Пользователь {}. Начато выполнение команды {}", getUserName(msg), Constants.EXIT_COMMAND);
        sendMessage(chatId, Constants.EXIT_MESSAGE);
        log.info("Пользователь {}. Отправлено сообщение: {}", getUserName(msg), Constants.EXIT_MESSAGE);
        log.info("Пользователь {}. Завершено выполнение команды {}", getUserName(msg), Constants.EXIT_COMMAND);
        currentState = TelegramBotState.WAITING_START;
    }

    private void handleRateCommand(String chatId, Message msg) {
        log.info("Пользователь {}. Начато выполнение команды {}", getUserName(msg), Constants.RATE_COMMAND);
        ReplyKeyboard keyboard = replyKeyboardGenerator.getCurrencyKeyboard();
        sendMessageWithReplyKeyboard(chatId, Constants.RATE_MESSAGE, keyboard);
        log.info("Пользователь {}. Отправлено сообщение: {}", getUserName(msg), Constants.RATE_MESSAGE);
        command = new Command();
        selectedCurrencies = new ArrayList<>();
        currentState = TelegramBotState.WAITING_CURRENCY_CODES;
    }

    private void handleMessage(Update update) {
        Message message = update.getMessage();
        String chatId = update.getMessage().getChatId().toString();

        switch (currentState) {
            case WAITING_CURRENCY_CODES -> handleCurrencyCodeSelection(chatId, message);
            case WAITING_ALGORITHM -> handleAlgorithmSelection(chatId, message);
            case WAITING_DATE_OR_PERIOD -> handleDateOrPeriodSelection(chatId, message);
            case WAITING_PERIOD -> handlePeriodSelection(chatId, message);
            case WAITING_DATE -> handleDateSelection(chatId, message);
            case WAITING_OUTPUT_TYPE -> handleOutputTypeSelection(chatId, message);
        }
    }

    private void handleCurrencyCodeSelection(String chatId, Message message) {
        String messageText = message.getText();
        log.info("Пользователь {}. Получено сообщение: {}", getUserName(message), messageText);
        if (messageText.equals(Constants.FINISH_BUTTON)) {
            if (selectedCurrencies.isEmpty()) {
                String telegramMessage = Constants.NO_SELECTED_CURRENCY_ERROR_MESSAGE;
                sendMessage(chatId, telegramMessage);
                log.info("Пользователь {}. Отправлено сообщение: {}", getUserName(message), telegramMessage);
            } else {
                String telegramMessage = Constants.SELECT_ALGORITHM_MESSAGE;
                ReplyKeyboard keyboard = replyKeyboardGenerator.getAlgorithmKeyboard();
                sendMessageWithReplyKeyboard(chatId, telegramMessage, keyboard);
                log.info("Пользователь {}. Отправлено сообщение: {}", getUserName(message), telegramMessage);
                currentState = TelegramBotState.WAITING_ALGORITHM;
            }
        } else {
            try {
                CurrencyCode currencyCode = CurrencyCode.valueOf(messageText);
                if (!selectedCurrencies.contains(currencyCode)) {
                    selectedCurrencies.add(currencyCode);
                }
                String telegramMessage = Constants.CURRENCY_CONFIRM_MESSAGE + selectedCurrencies;
                sendMessage(chatId, telegramMessage);
                log.info("Пользователь {}. Отправлено сообщение: {}", getUserName(message), telegramMessage);
            } catch (IllegalArgumentException e) {
                String telegramMessage = Constants.NO_SUCH_CURRENCY_ERROR_MESSAGE;
                sendMessage(chatId, telegramMessage);
                log.info("Пользователь {}. Отправлено сообщение: {}", getUserName(message), telegramMessage);
            }
        }
    }

    private void handleAlgorithmSelection(String chatId, Message message) {
        String messageText = message.getText();
        log.info("Пользователь {}. Получено сообщение: {}", getUserName(message), messageText);
        try {
            AlgorithmType algorithmType = AlgorithmType.valueOf(messageText);
            command.setAlgorithmType(algorithmType);
            ReplyKeyboard keyboard = replyKeyboardGenerator.getPeriodOrDateKeyboard();
            String telegramMessage = Constants.SELECT_DATE_OR_PERIOD_MESSAGE;
            sendMessageWithReplyKeyboard(chatId, telegramMessage, keyboard);
            log.info("Пользователь {}. Отправлено сообщение: {}", getUserName(message), telegramMessage);
            command.setCurrencyCodes(selectedCurrencies);
            currentState = TelegramBotState.WAITING_DATE_OR_PERIOD;
        } catch (IllegalArgumentException e) {
            String telegramMessage = Constants.NO_SUCH_ALGORITHM_ERROR_MESSAGE;
            sendMessage(chatId, telegramMessage);
            log.info("Пользователь {}. Отправлено сообщение: {}", getUserName(message), telegramMessage);
        }
    }

    private void handleDateOrPeriodSelection(String chatId, Message message) {
        String messageText = message.getText();
        log.info("Пользователь {}. Получено сообщение: {}", getUserName(message), messageText);
        try {
            ForecastDuration forecastDuration = ForecastDuration.valueOf(messageText);
            switch (forecastDuration) {
                case DATE -> {
                    ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove(true);
                    String telegramMessage = Constants.SELECT_DATE_MESSAGE;
                    sendMessageWithReplyKeyboard(chatId, telegramMessage, replyKeyboardRemove);
                    log.info("Пользователь {}. Отправлено сообщение: {}", getUserName(message), telegramMessage);
                    currentState = TelegramBotState.WAITING_DATE;
                }
                case PERIOD -> {
                    ReplyKeyboard keyboard = replyKeyboardGenerator.getPeriodKeyboard();
                    String telegramMessage = Constants.SELECT_PERIOD_MESSAGE;
                    sendMessageWithReplyKeyboard(chatId, telegramMessage, keyboard);
                    log.info("Пользователь {}. Отправлено сообщение: {}", getUserName(message), telegramMessage);
                    currentState = TelegramBotState.WAITING_PERIOD;
                }
            }
        } catch (IllegalArgumentException e) {
            String telegramMessage = Constants.DATE_OR_PERIOD_ERROR_MESSAGE;
            sendMessage(chatId, telegramMessage);
            log.info("Пользователь {}. Отправлено сообщение: {}", getUserName(message), telegramMessage);
        }
    }

    private void handleDateSelection(String chatId, Message message) {
        String messageText = message.getText();
        log.info("Пользователь {}. Получено сообщение: {}", getUserName(message), messageText);
        LocalDate date = null;
        if (messageText.equalsIgnoreCase("tomorrow")) {
            date = LocalDate.now().plusDays(1);
        } else {
            try {
                date = LocalDate.parse(messageText, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                log.error("Ошибка парсинга даты: {}. {}", messageText, e.getMessage());
            }
        }

        if (date != null && date.isAfter(LocalDate.now())) {
            command.setStartDate(date);
            command.setEndDate(date);
            command.setOutputType(OutputType.LIST);
            sendCurrencyForecast(chatId, message);
        } else {
            String telegramMessage = Constants.SELECT_DATE_ERROR_MESSAGE;
            sendMessage(chatId, telegramMessage);
            log.info("Пользователь {}. Отправлено сообщение: {}", getUserName(message), telegramMessage);
        }
    }

    private void handlePeriodSelection(String chatId, Message message) {
        String messageText = message.getText();
        log.info("Пользователь {}. Получено сообщение: {}", getUserName(message), messageText);
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
            String telegramMessage = Constants.SELECT_OUTPUT_TYPE_MESSAGE;
            sendMessageWithReplyKeyboard(chatId, telegramMessage, keyboard);
            log.info("Пользователь {}. Отправлено сообщение: {}", getUserName(message), telegramMessage);
            currentState = TelegramBotState.WAITING_OUTPUT_TYPE;
        } catch (IllegalArgumentException e) {
            String telegramMessage = Constants.NO_SUCH_PERIOD_ERROR_MESSAGE;
            sendMessage(chatId, telegramMessage);
            log.info("Пользователь {}. Отправлено сообщение: {}", getUserName(message), telegramMessage);
        }
    }

    private void handleOutputTypeSelection(String chatId, Message message) {
        String messageText = message.getText();
        log.info("Пользователь {}. Получено сообщение: {}", getUserName(message), messageText);
        try {
            OutputType outputType = OutputType.valueOf(messageText);
            command.setOutputType(outputType);
            sendCurrencyForecast(chatId, message);
        } catch (IllegalArgumentException e) {
            String telegramMessage = Constants.NO_SUCH_OUTPUT_TYPE_ERROR_MESSAGE;
            sendMessage(chatId, telegramMessage);
            log.info("Пользователь {}. Отправлено сообщение: {}", getUserName(message), telegramMessage);
        }
    }

    private void sendCurrencyForecast(String chatId, Message message) {
        try {
            Map<String, List<Currency>> forecastedCurrency = forecastingService.calculateCurrencyRates(command);

            switch (command.getOutputType()) {
                case LIST -> {
                    ListOutputGenerator listOutputGenerator = new ListOutputGenerator();
                    String currencies = listOutputGenerator.createList(forecastedCurrency);
                    sendMessage(chatId, currencies);
                    log.info("Пользователь {}. Отправлено сообщение: {}", getUserName(message), currencies);
                }
                case GRAPH -> {
                    ChartOutputGenerator chartOutputGenerator = new ChartOutputGenerator();
                    byte[] chart = chartOutputGenerator.createChart(forecastedCurrency);
                    SendPhoto chartMessage = sendMessageGenerator.createPhotoMessage(chatId, chart);
                    executeMessage(chartMessage);
                    log.info("Пользователь {}. Отправлено сообщение c графиком курсов валют", getUserName(message));
                }
            }
            ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove(true);
            sendMessageWithReplyKeyboard(chatId, Constants.FINISH_MESSAGE, replyKeyboardRemove);
            log.info("Пользователь {}. Отправлено сообщение: {}", getUserName(message), Constants.FINISH_MESSAGE);
            log.info("Пользователь {}. Завершено выполнение команды {}", getUserName(message), Constants.RATE_COMMAND);
            currentState = TelegramBotState.WAITING_START;
        } catch (PredictionDataException e) {
            ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove(true);
            String telegramMessage = Constants.NO_PREDICTION_DATA_ERROR_MESSAGE;
            sendMessageWithReplyKeyboard(chatId, telegramMessage, replyKeyboardRemove);
            log.info("Пользователь {}. Отправлено сообщение: {}", getUserName(message), telegramMessage);
            currentState = TelegramBotState.WAITING_START;
        }
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = sendMessageGenerator.createTextMessage(chatId, text);
        executeMessage(message);
    }

    /**
     * Отправляет текстовое сообщение с пользовательской клавиатурой через Telegram API.
     *
     * @param chatId   Идентификатор чата.
     * @param text     Текст сообщения.
     * @param keyboard Объект пользовательской клавиатуры.
     */
    private void sendMessageWithReplyKeyboard(String chatId, String text, ReplyKeyboard keyboard) {
        SendMessage message = sendMessageGenerator.createTextMessageWithReplyKeyboard(chatId, text, keyboard);
        executeMessage(message);
    }

    /**
     * Отправляет текстовое сообщение через Telegram API.
     *
     * @param message Объект SendMessage для отправки текстового сообщения.
     */
    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения пользователю в Telegram. {}", e.getMessage());
            throw new TelegramException("Ошибка при отправке сообщения пользователю в Telegram");
        }
    }

    /**
     * Отправляет сообщение с фотографией через Telegram API.
     *
     * @param message Объект SendPhoto для отправки сообщения с изображением.
     */
    private void executeMessage(SendPhoto message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения пользователю в Telegram. {}", e.getMessage());
            throw new TelegramException("Ошибка при отправке сообщения пользователю в Telegram");
        }
    }

    /**
     * Метод для получения имени пользователя из сообщения.
     *
     * @param msg Сообщение.
     * @return Никнейм пользователя. Если никнейм не заполнен - фамилия и имя пользователя.
     */
    public String getUserName(Message msg) {
        User user = msg.getFrom();
        return (user.getUserName() != null) ? user.getUserName() :
                String.format("%s %s", user.getLastName(), user.getFirstName());
    }
}
