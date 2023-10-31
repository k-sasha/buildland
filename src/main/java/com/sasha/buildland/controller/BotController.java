package com.sasha.buildland.controller;

import com.sasha.buildland.config.BotConfig;
import com.sasha.buildland.entity.Forklift;
import com.sasha.buildland.service.ForkliftService;
import com.sasha.buildland.service.ForkliftServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BotController extends TelegramLongPollingBot {

    @Autowired
    final BotConfig config;

    @Autowired
    private ForkliftService forkliftService;

    private Map<Long, Forklift> usersForkliftMap = new HashMap<>();
    private Map<Long, String> usersCurrentActionMap = new HashMap<>();

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
        if (update.hasMessage() && update.getMessage().hasText()) { //if we got text
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            // Check if it's a command (typically starts with '/')
            if (messageText.startsWith("/")) {
                handleCommand(chatId, messageText, update);
            } else {
                handleUserResponse(chatId, messageText);
            }
        }
    }

    private void handleCommand(long chatId, String command, Update update) {
        switch (command) {
            case "/start":
                // get a welcome message
                startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                break;
            case "/addForklift":
                addForkliftCommandReceived(chatId);
                break;
            default:
                prepareAndSendMessage(chatId, "Sorry, the command was not recognized");
        }
    }

    private void startCommandReceived(long chatId, String name) {
        // welcome message
        String answer = "Hi, "+ name+" nice to meet you";
        prepareAndSendMessage(chatId, answer);

        // invoke method with keyboard
        sendMessageWithKeyboard(chatId, answer, createKeyboard());
    }

    private void sendMessageWithKeyboard(long chatId, String textToSend, ReplyKeyboardMarkup keyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        message.setReplyMarkup(keyboardMarkup); //attach the keyboard to our message

        executeMessage(message); // execute the message sending

    }

    private ReplyKeyboardMarkup createKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        //create a list of rows
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        // first row of buttons
        KeyboardRow row = new KeyboardRow();
        row.add("add");
        row.add("delete");
        keyboardRows.add(row); // add the row to the list

        // second row of buttons
        row = new KeyboardRow();
        row.add("update forklift");
        row.add("get forklift");
        keyboardRows.add(row); // add the row to the list

        // add to the keyboard
        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;
    }


    private void prepareAndSendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {

        }
    }

    private void addForkliftCommandReceived(long chatId) {
        usersForkliftMap.put(chatId, new Forklift());
        usersCurrentActionMap.put(chatId, "set_manufacturer");
        prepareAndSendMessage(chatId, "Please set the manufacturer:");
    }

    private void handleUserResponse(long chatId, String response) {
        Forklift forklift = usersForkliftMap.get(chatId);
        if (forklift == null) {
            prepareAndSendMessage(chatId, "Sorry, the command was not recognized");
            return;
        }

        switch (usersCurrentActionMap.get(chatId)) {
            case "set_manufacturer":
                forklift.setManufacturer(response);
                usersCurrentActionMap.put(chatId, "set_model");
                prepareAndSendMessage(chatId, "Please set the model:");
                break;
            case "set_model":
                forklift.setModel(response);
                usersCurrentActionMap.put(chatId, "set_capacity");
                prepareAndSendMessage(chatId, "Please set the capacity:");
                break;
            case "set_capacity":
                try {
                    int capacity = Integer.parseInt(response);
                    forklift.setCapacity(capacity);
                    usersCurrentActionMap.put(chatId, "set_year");
                    prepareAndSendMessage(chatId, "Please set the year:");
                } catch (NumberFormatException e) {
                    prepareAndSendMessage(chatId, "Invalid capacity. Please enter a valid number.");
                }
                break;
            case "set_year":
                try {
                    int year = Integer.parseInt(response);
                    forklift.setYear(year);
                    usersCurrentActionMap.put(chatId, "set_hours");
                    prepareAndSendMessage(chatId, "Please set the hours:");
                } catch (NumberFormatException e) {
                    prepareAndSendMessage(chatId, "Invalid year. Please enter a valid number.");
                }
                break;
            case "set_hours":
                try {
                    Long hours = Long.parseLong(response);
                    forklift.setHours(hours);
                    usersCurrentActionMap.put(chatId, "set_location");
                    prepareAndSendMessage(chatId, "Please set the location:");
                } catch (NumberFormatException e) {
                    prepareAndSendMessage(chatId, "Invalid hours. Please enter a valid number.");
                }
                break;
            case "set_location":
                forklift.setLocation(response);
                usersCurrentActionMap.put(chatId, "set_status");
                prepareAndSendMessage(chatId, "Please set the status:");
                break;
            case "set_status":
                forklift.setStatus(response);
                forkliftService.saveForklift(forklift);
                usersForkliftMap.remove(chatId);
                usersCurrentActionMap.remove(chatId);
                prepareAndSendMessage(chatId, "Forklift has been added successfully!");
                break;
            default:
                prepareAndSendMessage(chatId, "Sorry, I didn't understand that.");
        }
    }

}

