package com.sasha.buildland.controller;

import com.sasha.buildland.config.BotConfig;

import com.sasha.buildland.utils.ForkliftManagementHelper;
import com.sasha.buildland.utils.LocationManagementHelper;
import com.sasha.buildland.utils.KeyboardHelper;
import com.sasha.buildland.utils.ManufacturerManagementHelper;
import com.sasha.buildland.utils.MessageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
@Slf4j
public class BotController extends TelegramLongPollingBot {

    @Autowired
    final BotConfig config;

    @Autowired
    private ForkliftManagementHelper forkliftManagementHelper;

    @Autowired
    private LocationManagementHelper locationManagementHelper;

    @Autowired
    private ManufacturerManagementHelper manufacturerManagementHelper;

    @Autowired
    private KeyboardHelper keyboardHelper;

    @Autowired
    private MessageHelper messageHelper;

    private Map<Long, String> userStates = new HashMap<>();

    private static final String COMMAND_NOT_RECOGNIZED_MESSAGE = "Sorry, the command was not recognized";
    private static final String ADD_FORKLIFT = "add_forklift";
    private static final String GET_FORKLIFT = "get_forklift";
    private static final String FIND_ALL_FORKLIFTS_BY_CAPACITY = "find_forklift_by_capacity";
    private static final String FIND_ALL_FORKLIFTS_BY_PRICE = "find_forklift_by_price";
    private static final String ADD_LOCATION = "add_location";
    private static final String DELETE_LOCATION = "delete_location";
    private static final String ADD_MANUFACTURER = "add_manufacturer";
    private static final String DELETE_MANUFACTURER = "delete_manufacturer";
    private static final String COMPLETED = "completed";

    public BotController(BotConfig config) {
        this.config = config;
        // create menu
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "get a welcome message"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot command list: " + e.getMessage());
        }
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
                userStates.put(chatId, ADD_FORKLIFT);
            } else if (messageText.equals("add location")) {
                locationManagementHelper.addLocationCommandReceived(chatId);
                userStates.put(chatId, ADD_LOCATION);
            } else if (messageText.equals("add manufacturer")) {
                manufacturerManagementHelper.addManufacturerCommandReceived(chatId);
                userStates.put(chatId, ADD_MANUFACTURER);
            } else if (messageText.equals("delete")) { // if button pushed with start keyboard
                messageHelper.sendMessageWithKeyboard(chatId, "what do you want to delete?", keyboardHelper.createDeleteKeyboard());
            } else if (messageText.equals("delete location")) {
                locationManagementHelper.deleteLocationCommandReceived(chatId);
                userStates.put(chatId, DELETE_LOCATION);
            } else if (messageText.equals("delete manufacturer")) {
                manufacturerManagementHelper.deleteManufacturerCommandReceived(chatId);
                userStates.put(chatId, DELETE_MANUFACTURER);
            } else if (messageText.equals("get forklift")) { // if button pushed with start keyboard
                messageHelper.sendMessageWithKeyboard(chatId, "By what parameters do you want to get a forklift?", keyboardHelper.createGetKeyboard());
            } else if (messageText.equals("get all forklifts")) {
                userStates.put(chatId, GET_FORKLIFT);
                forkliftManagementHelper.getAllForkliftsCommandReceived(chatId);
                if (COMPLETED.equals(forkliftManagementHelper.getUsersCurrentActionMap().get(chatId))) {
                    userStates.remove(chatId); // Remove the user's state
                    forkliftManagementHelper.getUsersCurrentActionMap().remove(chatId); // Also remove the state from the helper
                }
            } else if (messageText.equals("find by capacity")) {
                userStates.put(chatId, FIND_ALL_FORKLIFTS_BY_CAPACITY);
                forkliftManagementHelper.findAllForkliftsByCapacityCommandReceived(chatId);

            } else if (messageText.equals("find by price")) {
                userStates.put(chatId, FIND_ALL_FORKLIFTS_BY_PRICE);
                forkliftManagementHelper.findAllForkliftsByPriceCommandReceived(chatId);

            } else {
                if (ADD_FORKLIFT.equals(userStates.get(chatId))) {
                    forkliftManagementHelper.handleAddForkliftResponse(chatId, messageText);
                    if (COMPLETED.equals(forkliftManagementHelper.getUsersCurrentActionMap().get(chatId))) {
                        userStates.remove(chatId); // Remove the user's state
                        forkliftManagementHelper.getUsersCurrentActionMap().remove(chatId); // Also remove the state from the helper
                    }
                } else if (FIND_ALL_FORKLIFTS_BY_CAPACITY.equals(userStates.get(chatId))) {
                    forkliftManagementHelper.handleSearchForkliftByCapacityResponse(chatId, messageText);
                    if (COMPLETED.equals(forkliftManagementHelper.getUsersCurrentActionMap().get(chatId))) {
                        userStates.remove(chatId); // Remove the user's state
                        forkliftManagementHelper.getUsersCurrentActionMap().remove(chatId); // Also remove the state from the helper
                    }
                }  else if (FIND_ALL_FORKLIFTS_BY_PRICE.equals(userStates.get(chatId))) {
                    forkliftManagementHelper.handleSearchForkliftByPriceResponse(chatId, messageText);
                    if (COMPLETED.equals(forkliftManagementHelper.getUsersCurrentActionMap().get(chatId))) {
                        userStates.remove(chatId); // Remove the user's state
                        forkliftManagementHelper.getUsersCurrentActionMap().remove(chatId); // Also remove the state from the helper
                    }
                }else if (ADD_LOCATION.equals(userStates.get(chatId))) {
                    locationManagementHelper.handleUserResponse(chatId, messageText);
                    if (COMPLETED.equals(locationManagementHelper.getUsersCurrentActionMap().get(chatId))) {
                        userStates.remove(chatId); // Remove the user's state
                        locationManagementHelper.getUsersCurrentActionMap().remove(chatId); // Also remove the state from the helper
                    }
                } else if (ADD_MANUFACTURER.equals(userStates.get(chatId))) {
                    manufacturerManagementHelper.handleUserResponse(chatId, messageText);
                    if (COMPLETED.equals(manufacturerManagementHelper.getUsersCurrentActionMap().get(chatId))) {
                        userStates.remove(chatId); // Remove the user's state
                        manufacturerManagementHelper.getUsersCurrentActionMap().remove(chatId); // Also remove the state from the helper
                    }
                }
            }
        } else if (update.hasCallbackQuery()) { // if we got VALUE (for example button id "Toyota", "Nissan" and etc).
            // CallbackQuery - this is query from buttons "Toyota", "Nissan"
            String callBackData = update.getCallbackQuery().getData(); // Toyota has "TOYOTA_BUTTON", Nissan has "NISSAN_BUTTON"
            // get messageId to edit text avoid sending new. If we know id than we can edit text
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            log.info("Callback query received from chatId: {}. Data: {}", chatId, callBackData);

            if (ADD_FORKLIFT.equals(userStates.get(chatId))) {
                forkliftManagementHelper.handleAddForkliftResponseWithInlineKeyboard(chatId, callBackData, messageId);
            } else if (DELETE_LOCATION.equals(userStates.get(chatId))) {
                locationManagementHelper.handleUserResponseWithInlineKeyboard(chatId, callBackData, messageId);
                if (COMPLETED.equals(locationManagementHelper.getUsersCurrentActionMap().get(chatId))) {
                    userStates.remove(chatId); // Remove the user's state
                    locationManagementHelper.getUsersCurrentActionMap().remove(chatId); // Also remove the state from the helper
                }
            } else if (DELETE_MANUFACTURER.equals(userStates.get(chatId))) {
                manufacturerManagementHelper.handleUserResponseWithInlineKeyboard(chatId, callBackData, messageId);
                if (COMPLETED.equals(manufacturerManagementHelper.getUsersCurrentActionMap().get(chatId))) {
                    userStates.remove(chatId); // Remove the user's state
                    manufacturerManagementHelper.getUsersCurrentActionMap().remove(chatId); // Also remove the state from the helper
                }
            }

        }

    }

    private void handleCommand(long chatId, String command, Update update) {
        if ("/start".equals(command)) {// get a welcome message
            startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
        } else {
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

