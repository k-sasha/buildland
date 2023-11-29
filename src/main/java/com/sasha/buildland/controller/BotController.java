package com.sasha.buildland.controller;

import com.sasha.buildland.config.BotConfig;
import com.sasha.buildland.utils.ForkliftManagementHelper;
import com.sasha.buildland.utils.KeyboardHelper;
import com.sasha.buildland.utils.MessageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;


@Component
@Slf4j
public class BotController extends TelegramLongPollingBot {

    @Autowired
    final BotConfig config;

    @Autowired
    private ForkliftManagementHelper forkliftManagementHelper;

    @Autowired
    private KeyboardHelper keyboardHelper;

    @Autowired
    private MessageHelper messageHelper;


    private static final String COMMAND_NOT_RECOGNIZED_MESSAGE = "Sorry, the command was not recognized";

    public BotController(BotConfig config) {
        this.config = config;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) { //if we got TEXT
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            log.info("Text message received from chatId: {}. Message: {}", chatId, messageText);

            // Check if it's a command (typically starts with '/')
            if (messageText.startsWith("/")) {
                handleCommand(chatId, messageText, update);
            } else if (messageText.equals("add")) { // if button pushed with start keyboard
                messageHelper.sendMessageWithKeyboard(chatId, "what do you want to add?", keyboardHelper.createAddKeyboard());
            } else if (messageText.equals("add forklift")) {
                forkliftManagementHelper.addForkliftCommandReceived(chatId);
        }else {
                forkliftManagementHelper.handleUserResponse(chatId, messageText);
            }
        } else if (update.hasCallbackQuery()) { // if we got VALUE (for example button id "Toyota", "Nissan" and etc).
            // CallbackQuery - this is query from buttons "Toyota", "Nissan"
            String callBackData = update.getCallbackQuery().getData(); // Toyota has "TOYOTA_BUTTON", Nissan has "NISSAN_BUTTON"
            // get messageId to edit text avoid sending new. If we know id than we can edit text
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            log.info("Callback query received from chatId: {}. Data: {}", chatId, callBackData);

            forkliftManagementHelper.handleUserResponseWithInlineKeyboard(chatId,callBackData,messageId);

        }
    }

    private void handleCommand(long chatId, String command, Update update) {
        switch (command) {
            case "/start":
                // get a welcome message
                startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                break;
            case "/addForklift":
                forkliftManagementHelper.addForkliftCommandReceived(chatId);
                break;
            default:
                messageHelper.prepareAndSendMessage(chatId, COMMAND_NOT_RECOGNIZED_MESSAGE);
                log.warn("Unrecognized command '{}' from chatId: {}", command, chatId);
        }
    }

    private void startCommandReceived(long chatId, String name) {
        // welcome message
        String answer = "Hi, " + name + " nice to meet you";
        log.info("Replied to user {}", name);

        // invoke method with keyboard
        messageHelper.sendMessageWithKeyboard(chatId, answer, keyboardHelper.createStartKeyboard());
    }
}

