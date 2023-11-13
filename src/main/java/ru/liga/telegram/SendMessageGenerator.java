package ru.liga.telegram;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.io.ByteArrayInputStream;

public class SendMessageGenerator {

    /**
     * Создает текстовое сообщение.
     *
     * @param chatId  Идентификатор чата.
     * @param message Текст сообщения.
     * @return Объект SendMessage для отправки текстового сообщения.
     */
    public SendMessage createTextMessage(String chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        return sendMessage;
    }

    /**
     * Создает текстовое сообщение с пользовательской клавиатурой.
     *
     * @param chatId   Идентификатор чата.
     * @param message  Текст сообщения.
     * @param keyboard Объект ReplyKeyboard для добавления пользовательской клавиатуры.
     * @return Объект SendMessage для отправки текстового сообщения с клавиатурой.
     */
    public SendMessage createTextMessageWithReplyKeyboard(String chatId, String message, ReplyKeyboard keyboard) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(keyboard);
        return sendMessage;
    }

    /**
     * Создает сообщение с изображением.
     *
     * @param chatId Идентификатор чата.
     * @param image  Байтовый массив с изображением.
     * @return Объект SendPhoto для отправки сообщения с изображением.
     */
    public SendPhoto createPhotoMessage(String chatId, byte[] image) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(new ByteArrayInputStream(image), "chatMsg"));
        return sendPhoto;
    }
}