package ru.liga.telegram;

import lombok.extern.log4j.Log4j2;
import ru.liga.domain.Command;
import ru.liga.enums.TelegramBotState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class UserBotState {
    private final Map<Long, TelegramBotState> userBotStates = new ConcurrentHashMap<>();
    private final Map<Long, Command> userCommands = new ConcurrentHashMap<>();

    public void setUserBotState(long userId, TelegramBotState state) {
        log.info("Установка состояния для пользователя с Id = {}: {}", userId, state);
        userBotStates.put(userId, state);
    }

    public TelegramBotState getUserBotState(long userId) {
        TelegramBotState state = userBotStates.getOrDefault(userId, TelegramBotState.WAITING_START);
        log.info("Получено состояние для пользователя с Id = {}: {}", userId, state);
        return state;
    }

    public Command getUserCommand(long userId) {
        return userCommands.computeIfAbsent(userId, k -> new Command());
    }
}
