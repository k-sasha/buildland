package com.sasha.buildland.utils;

import com.sasha.buildland.entity.InlineKeyboardObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
@Slf4j
public class MessageHelper {

    private final TelegramLongPollingBot bot;

    @Autowired
    @Lazy
    private KeyboardHelper keyboardHelper;

    @Autowired
    public MessageHelper(@Lazy TelegramLongPollingBot bot) {
        this.bot = bot;
    }

    public void sendMessageWithKeyboard(long chatId, String textToSend, ReplyKeyboardMarkup keyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        message.setReplyMarkup(keyboardMarkup); //attach the keyboard to our message

        log.info("Sending message with keyboard to chatId {}: {}", chatId, textToSend);
        executeMessage(message); // execute the message sending
    }

    public void prepareAndSendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        log.info("Preparing and sending message with keyboard to chatId {}: {}", chatId, textToSend);
        executeMessage(message);
    }

    public void editAndSendMessage(long chatId, String text, long messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setMessageId((int) messageId);

        log.info("Editing and sending message with keyboard to chatId {}: {}", chatId, text);
        executeMessage(message);
    }

    public BotApiMethod<?> prepareMessage(long chatId, String text, Long messageId, boolean isEdit) {
        BotApiMethod<?> message;
        if (isEdit) {
            EditMessageText editMessage = new EditMessageText();
            editMessage.setChatId(String.valueOf(chatId));
            editMessage.setText(text);
            editMessage.setMessageId(messageId.intValue());
            message = editMessage;
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(chatId));
            sendMessage.setText(text);
            message = sendMessage;
        }
        return message;
    }

    public void sendMessageWithInlineKeyboard(List<String> list, String text
            , long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);   // Set the text of the message to be sent

        InlineKeyboardMarkup markupInline= keyboardHelper.createInlineKeyboard(list);

        // Set the reply markup for the message, which in this case is an inline keyboard
        // This markup defines the layout and buttons of the inline keyboard
        message.setReplyMarkup(markupInline);

        log.info("Preparing and sending message with inline keyboard to chatId {}: {}", chatId, text);
        executeMessage(message);  // send message
    }

    public void sendMessageWithInlineKeyboard(List<String> list, String text, long chatId
            , long messageId) {
        BotApiMethod<?> message = prepareMessage(chatId, text, messageId, true);

        InlineKeyboardMarkup markupInline= keyboardHelper.createInlineKeyboard(list);

        // check return type from prepareMessage() method
        if (message instanceof EditMessageText) {
            EditMessageText editMessage = (EditMessageText) message;
            editMessage.setReplyMarkup(markupInline);  // Set the reply markup for the message, which in this case is an inline keyboard
        } else if (message instanceof SendMessage) {
            SendMessage sendMessage = (SendMessage) message;
            sendMessage.setReplyMarkup(markupInline); // Set the reply markup for the message, which in this case is an inline keyboard
        }

        log.info("Preparing and sending message with inline keyboard to chatId {}: {}", chatId, text);
        executeMessage(message);  // send message
    }

    public void sendMessageConfirmationWithInlineKeyboard(String text, long chatId
            , long messageId) {
        BotApiMethod<?> message = prepareMessage(chatId, text, messageId, true);

        InlineKeyboardMarkup markupInline= keyboardHelper.createInlineKeyboardConfirmation();

        // check return type from prepareMessage() method
        if (message instanceof EditMessageText) {
            EditMessageText editMessage = (EditMessageText) message;
            editMessage.setReplyMarkup(markupInline);  // Set the reply markup for the message, which in this case is an inline keyboard
        } else if (message instanceof SendMessage) {
            SendMessage sendMessage = (SendMessage) message;
            sendMessage.setReplyMarkup(markupInline); // Set the reply markup for the message, which in this case is an inline keyboard
        }

        log.info("Preparing and sending message with inline keyboard to chatId {}: {}", chatId, text);
        executeMessage(message);  // send message
    }

    public void sendMessageWithInlineKeyboard2(List<? extends InlineKeyboardObject> objectNames, String text, long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);   // Set the text of the message to be sent

        InlineKeyboardMarkup markupInline = keyboardHelper.createInlineKeyboard2(objectNames);

        message.setReplyMarkup(markupInline);

        log.info("Preparing and sending message with inline keyboard to chatId {}: {}", chatId, text);
        executeMessage(message);
    }

    public void executeMessage(BotApiMethod<?> message) { // BotApiMethod<?> includes SendMessage and EditMessageText classes
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred while sending message: {}", e.getMessage());
        }
    }
}
