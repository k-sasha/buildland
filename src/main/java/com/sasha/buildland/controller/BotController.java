package com.sasha.buildland.controller;

import com.sasha.buildland.config.BotConfig;
import com.sasha.buildland.entity.Forklift;
import com.sasha.buildland.service.ForkliftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

@Component
public class BotController extends TelegramLongPollingBot {

    @Autowired
    final BotConfig config;

    @Autowired
    private ForkliftService forkliftService;

    private Map<Long, Forklift> usersForkliftMap = new HashMap<>();
    private Map<Long, String> usersCurrentActionMap = new HashMap<>();

    private List<String> manufacturers = Arrays.asList("Toyota", "Nissan", "Caterpillar");
    private List<String> locations = Arrays.asList("El Monte", "Commerce");
    private List<String> statuses = Arrays.asList("Ready for sale", "repairs needed", "sent for repair", "rented", "sold");

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

            // Check if it's a command (typically starts with '/')
            if (messageText.startsWith("/")) {
                handleCommand(chatId, messageText, update);
            } else if (messageText.equals("add")) { // if button pushed with start keyboard
                sendMessageWithKeyboard(chatId, "what do you want to add?", createAddKeyboard());
            } else if (messageText.equals("add forklift")) {
                addForkliftCommandReceived(chatId);
        }else {
                handleUserResponse(chatId, messageText);
            }
        } else if (update.hasCallbackQuery()) { // if we got VALUE (for example button id "Toyota", "Nissan" and etc).
            // CallbackQuery - this is query from buttons "Toyota", "Nissan"
            String callBackData = update.getCallbackQuery().getData(); // Toyota has "TOYOTA_BUTTON", Nissan has "NISSAN_BUTTON"
            // get messageId to edit text avoid sending new. If we know id than we can edit text
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            Forklift forklift = usersForkliftMap.get(chatId);
            if (forklift == null) {
                prepareAndSendMessage(chatId, "Sorry, the command was not recognized");
                return;
            }

            switch (usersCurrentActionMap.get(chatId)) {
                case "set_manufacturer":
                    // check which button we got
                    if (callBackData.equals("TOYOTA_BUTTON")) {
                        forklift.setManufacturer("Toyota");
                    } else if (callBackData.equals("NISSAN_BUTTON")) {
                        forklift.setManufacturer("Nissan");
                    } else if (callBackData.equals("CATERPILLAR_BUTTON")) {
                        forklift.setManufacturer("Caterpillar");
                    }
                    usersCurrentActionMap.put(chatId, "set_model");
                    editAndSendMessage(chatId, "Please set the model:", messageId);
                    break;
                case "set_location":
                    if (callBackData.equals("EL MONTE_BUTTON")) {
                        forklift.setLocation("El Monte");
                    } else if (callBackData.equals("COMMERCE_BUTTON")) {
                        forklift.setLocation("Commerce");
                    }
                    usersCurrentActionMap.put(chatId, "set_status");
                    String text = "Please set the status:";
                    sendMessageWithInlineKeyboard(statuses, text, chatId, messageId);
                    break;
                case "set_status":
                    if(callBackData.equals("READY FOR SALE_BUTTON")){
                        forklift.setStatus("Ready for sale");
                    } else if(callBackData.equals("REPAIRS NEEDED_BUTTON")){
                        forklift.setStatus("repairs needed");
                    }
                    forkliftService.saveForklift(forklift);
                    usersForkliftMap.remove(chatId);
                    usersCurrentActionMap.remove(chatId);
                    editAndSendMessage(chatId, "The details about the new forklift must be here.", messageId);
                    sendMessageWithKeyboard(chatId, "Forklift has been added successfully!", createStartKeyboard());
                    break;
                default:
                    prepareAndSendMessage(chatId, "Sorry, I didn't understand that.");
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
        String answer = "Hi, " + name + " nice to meet you";
//        prepareAndSendMessage(chatId, answer);

        // invoke method with keyboard
        sendMessageWithKeyboard(chatId, answer, createStartKeyboard());
    }

    private void sendMessageWithKeyboard(long chatId, String textToSend, ReplyKeyboardMarkup keyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        message.setReplyMarkup(keyboardMarkup); //attach the keyboard to our message

        executeMessage(message); // execute the message sending

    }


    private ReplyKeyboardMarkup createStartKeyboard() {
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

    private ReplyKeyboardMarkup createAddKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        //create a list of rows
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        // first row of buttons
        KeyboardRow row = new KeyboardRow();
        row.add("add forklift");
        row.add("add location");
        keyboardRows.add(row); // add the row to the list

        // second row of buttons
        row = new KeyboardRow();
        row.add("add manufacturer");
        row.add("main menu");
        keyboardRows.add(row); // add the row to the list

        // add to the keyboard
        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;
    }

    private ReplyKeyboardMarkup createReturnKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        //create a list of rows
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        // first row of buttons
        KeyboardRow row = new KeyboardRow();
        row.add("main menu");
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

    private void editAndSendMessage(long chatId, String text, long messageId) {

        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setMessageId((int) messageId);

        executeMessage(message);
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {

        }
    }

    private void executeMessage(EditMessageText message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {

        }
    }

    private void addForkliftCommandReceived(long chatId) {

        usersForkliftMap.put(chatId, new Forklift());
        usersCurrentActionMap.put(chatId, "set_manufacturer");
        String text = "Please set the manufacturer:";
//        prepareAndSendMessage(chatId, text);
        sendMessageWithKeyboard(chatId, "New forklift", createReturnKeyboard());
        sendMessageWithInlineKeyboard(manufacturers, text, chatId);
    }

    private void sendMessageWithInlineKeyboard(List<String> list, String text, long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);  // Text from method arguments

        //  create keyboard with buttons from List
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>(); // create rows

        // For each list's element
        for (int i = 0; i < list.size(); i++) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();  // create one row

            var firstButton = new InlineKeyboardButton();  // create first button
            firstButton.setText(list.get(i));
            firstButton.setCallbackData(list.get(i).toUpperCase() + "_BUTTON"); // indicator (id), that allow bot to understand which button is pressed
            rowInline.add(firstButton);

            // Check if there is the next element to add into the same row
            if (i + 1 < list.size()) {
                var secondButton = new InlineKeyboardButton();
                secondButton.setText(list.get(i + 1));
                secondButton.setCallbackData(list.get(i + 1).toUpperCase() + "_BUTTON");
                rowInline.add(secondButton);

                i++;  // skip next element because we have already added it
            }

            rowsInline.add(rowInline); // add row to rows
        }

        markupInline.setKeyboard(rowsInline); //add to the keyboard
        message.setReplyMarkup(markupInline); // add to the message

        executeMessage(message);  // send message
    }

    private void sendMessageWithInlineKeyboard(List<String> list, String text, long chatId, long messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setMessageId((int) messageId);

        //  create keyboard with buttons from List
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>(); // create rows

        // For each list's element
        for (int i = 0; i < list.size(); i++) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();  // create one row

            var firstButton = new InlineKeyboardButton();  // create first button
            firstButton.setText(list.get(i));
            firstButton.setCallbackData(list.get(i).toUpperCase() + "_BUTTON"); // indicator (id), that allow bot to understand which button is pressed
            rowInline.add(firstButton);

            // Check if there is the next element to add into the same row
            if (i + 1 < list.size()) {
                var secondButton = new InlineKeyboardButton();
                secondButton.setText(list.get(i + 1));
                secondButton.setCallbackData(list.get(i + 1).toUpperCase() + "_BUTTON");
                rowInline.add(secondButton);

                i++;  // skip next element because we have already added it
            }

            rowsInline.add(rowInline); // add row to rows
        }

        markupInline.setKeyboard(rowsInline); //add to the keyboard
        message.setReplyMarkup(markupInline); // add to the message

        executeMessage(message);  // send message
    }


    private void handleUserResponse(long chatId, String response) {
        Forklift forklift = usersForkliftMap.get(chatId);
        if (forklift == null) {
            prepareAndSendMessage(chatId, "Sorry, the command was not recognized");
            return;
        }

        switch (usersCurrentActionMap.get(chatId)) {
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
                    String text = "Please set the location:";
                    sendMessageWithInlineKeyboard(locations, text, chatId);
                } catch (NumberFormatException e) {
                    prepareAndSendMessage(chatId, "Invalid hours. Please enter a valid number.");
                }
                break;
            default:
                prepareAndSendMessage(chatId, "Sorry, I didn't understand that. Please click on the button");
        }
    }

}

