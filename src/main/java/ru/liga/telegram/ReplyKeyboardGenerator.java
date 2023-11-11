package ru.liga.telegram;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.liga.enums.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReplyKeyboardGenerator {
    /**
     * Получить клавиатуру для выбора валют.
     *
     * @return Клавиатура для выбора валют.
     */
    public ReplyKeyboard getCurrencyKeyboard() {
        KeyboardRow row = new KeyboardRow();
        CurrencyCode[] currencyCodes = CurrencyCode.values();
        for (CurrencyCode currencyCode : currencyCodes) {
            row.add(new KeyboardButton(currencyCode.toString()));
        }
        row.add(new KeyboardButton(Constants.FINISH_BUTTON));
        return getReplyKeyboardMarkup(row);
    }

    /**
     * Получить клавиатуру для выбора периода или даты прогнозирования.
     *
     * @return Клавиатура для выбора периода или даты прогнозирования.
     */
    public ReplyKeyboard getPeriodOrDateKeyboard() {
        KeyboardRow row = new KeyboardRow();
        ForecastDuration[] duration = ForecastDuration.values();
        for (ForecastDuration type : duration) {
            row.add(new KeyboardButton(type.toString()));
        }
        return getReplyKeyboardMarkup(row);
    }

    /**
     * Получить клавиатуру для выбора периода прогнозирования.
     *
     * @return Клавиатура для выбора периода прогнозирования.
     */
    public ReplyKeyboard getPeriodKeyboard() {
        KeyboardRow row = new KeyboardRow();
        ForecastPeriod[] forecastPeriods = ForecastPeriod.values();
        for (ForecastPeriod forecastPeriod : forecastPeriods) {
            row.add(new KeyboardButton(forecastPeriod.toString()));
        }
        return getReplyKeyboardMarkup(row);
    }

    /**
     * Получить клавиатуру для выбора алгоритма прогнозирования.
     *
     * @return Клавиатура для выбора алгоритма прогнозирования.
     */
    public ReplyKeyboard getAlgorithmKeyboard() {
        KeyboardRow row = new KeyboardRow();
        AlgorithmType[] algorithmTypes = AlgorithmType.values();
        for (AlgorithmType algorithmType : algorithmTypes) {
            row.add(new KeyboardButton(algorithmType.toString()));
        }
        return getReplyKeyboardMarkup(row);
    }


    /**
     * Получить клавиатуру для выбора типа вывода прогноза.
     *
     * @return Клавиатура для выбора типа вывода прогноза.
     */
    public ReplyKeyboard getOutputTypeKeyboard() {
        KeyboardRow row = new KeyboardRow();
        OutputType[] outputTypes = OutputType.values();
        for (OutputType outputType : outputTypes) {
            row.add(new KeyboardButton(outputType.toString()));
        }
        return getReplyKeyboardMarkup(row);
    }

    private ReplyKeyboardMarkup getReplyKeyboardMarkup(KeyboardRow... row) {
        List<KeyboardRow> keyboard = new ArrayList<>(Arrays.asList(row));

        final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        return replyKeyboardMarkup;
    }
}
