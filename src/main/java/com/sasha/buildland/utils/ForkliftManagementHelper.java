package com.sasha.buildland.utils;

import com.sasha.buildland.entity.Forklift;
import com.sasha.buildland.service.ForkliftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ForkliftManagementHelper {

    private final ForkliftService forkliftService;
    private final KeyboardHelper keyboardHelper;
    private final MessageHelper messageHelper;

    private Map<Long, Forklift> usersForkliftMap = new HashMap<>();
    private Map<Long, String> usersCurrentActionMap = new HashMap<>();

    private Map<String, String> buttonToLocationMap = new HashMap<>();
    private Map<String, String> buttonToManufacturerMap = new HashMap<>();
    private Map<String, String> buttonToStatusMap = new HashMap<>();

    private List<String> manufacturers = Arrays.asList("Toyota", "Nissan", "Caterpillar");
    private List<String> locations = Arrays.asList("El Monte", "Commerce");
    private List<String> statuses = Arrays.asList("ready for sale", "repairs needed", "sent for repair", "rented", "sold");

    private final String COMMAND_NOT_RECOGNIZED_MESSAGE = "Sorry, the command was not recognized";

    @Autowired
    public ForkliftManagementHelper(ForkliftService forkliftService,
                                    @Lazy KeyboardHelper keyboardHelper,
                                    @Lazy MessageHelper messageHelper) {
        this.forkliftService = forkliftService;
        this.keyboardHelper = keyboardHelper;
        this.messageHelper = messageHelper;
    }

    {   buttonToManufacturerMap.put("TOYOTA_BUTTON", "Toyota");
        buttonToManufacturerMap.put("NISSAN_BUTTON", "Nissan");
        buttonToManufacturerMap.put("CATERPILLAR_BUTTON", "Caterpillar");

        buttonToLocationMap.put("EL MONTE_BUTTON","El Monte");
        buttonToLocationMap.put("COMMERCE_BUTTON","Commerce");

        buttonToStatusMap.put("READY FOR SALE_BUTTON", "Ready for sale");
        buttonToStatusMap.put("REPAIRS NEEDED_BUTTON", "repairs needed");
        buttonToStatusMap.put("SEND FOR REPAIR_BUTTON", "sent for repair");
        buttonToStatusMap.put("RENTED_BUTTON", "rented");
        buttonToStatusMap.put("SOLD_BUTTON", "sold");
    }


    public void addForkliftCommandReceived(long chatId) {

        usersForkliftMap.put(chatId, new Forklift());
        usersCurrentActionMap.put(chatId, "set_manufacturer");
        String text = "Please set the manufacturer:";
        messageHelper.sendMessageWithKeyboard(chatId, "New forklift", keyboardHelper.createReturnKeyboard());
        messageHelper.sendMessageWithInlineKeyboard(manufacturers, text, chatId);
    }

    public void handleUserResponse(long chatId, String response) {
        Forklift forklift = usersForkliftMap.get(chatId);
        if (forklift == null) {
            messageHelper.prepareAndSendMessage(chatId, COMMAND_NOT_RECOGNIZED_MESSAGE);
            return;
        }

        switch (usersCurrentActionMap.get(chatId)) {
            case "set_model":
                forklift.setModel(response);
                usersCurrentActionMap.put(chatId, "set_capacity");
                messageHelper.prepareAndSendMessage(chatId, "Please set the capacity:");
                break;
            case "set_capacity":
                try {
                    int capacity = Integer.parseInt(response);
                    forklift.setCapacity(capacity);
                    usersCurrentActionMap.put(chatId, "set_year");
                    messageHelper.prepareAndSendMessage(chatId, "Please set the year:");
                } catch (NumberFormatException e) {
                    messageHelper.prepareAndSendMessage(chatId, "Invalid capacity. Please enter a valid number.");
                }
                break;
            case "set_year":
                try {
                    int year = Integer.parseInt(response);
                    forklift.setYear(year);
                    usersCurrentActionMap.put(chatId, "set_hours");
                    messageHelper.prepareAndSendMessage(chatId, "Please set the hours:");
                } catch (NumberFormatException e) {
                    messageHelper.prepareAndSendMessage(chatId, "Invalid year. Please enter a valid number.");
                }
                break;
            case "set_hours":
                try {
                    Long hours = Long.parseLong(response);
                    forklift.setHours(hours);
                    usersCurrentActionMap.put(chatId, "set_location");
                    String text = "Please set the location:";
                    messageHelper.sendMessageWithInlineKeyboard(locations, text, chatId);
                } catch (NumberFormatException e) {
                    messageHelper.prepareAndSendMessage(chatId, "Invalid hours. Please enter a valid number.");
                }
                break;
            default:
                messageHelper.prepareAndSendMessage(chatId, "Sorry, I didn't understand that. Please click on the button");
        }
    }

    public void handleUserResponseWithInlineKeyboard(long chatId, String callBackData, long messageId ) {
        Forklift forklift = usersForkliftMap.get(chatId);
        if (forklift == null) {
            messageHelper.prepareAndSendMessage(chatId, COMMAND_NOT_RECOGNIZED_MESSAGE);
            return;
        }

        switch (usersCurrentActionMap.get(chatId)) {
            case "set_manufacturer":
                String manufacturer = buttonToManufacturerMap.get(callBackData);
                if (manufacturer != null) {
                    forklift.setManufacturer(manufacturer);
                    usersCurrentActionMap.put(chatId, "set_model");
                    messageHelper.editAndSendMessage(chatId, "Please set the model:", messageId);
                } else {
                    messageHelper.prepareAndSendMessage(chatId, "Sorry, the manufacturer was not recognized");
                }
                break;
            case "set_location":
                String location = buttonToLocationMap.get(callBackData);
                if (location != null) {
                    forklift.setLocation(location);
                    usersCurrentActionMap.put(chatId, "set_status");
                    messageHelper.sendMessageWithInlineKeyboard(statuses, "Please set the status:", chatId, messageId);
                } else {
                    messageHelper.prepareAndSendMessage(chatId, "Sorry, the location was not recognized");
                }
                break;
            case "set_status":
                String status = buttonToStatusMap.get(callBackData);
                if (status != null) {
                    forklift.setStatus(status);
                    forkliftService.saveForklift(forklift);
                    usersForkliftMap.remove(chatId);
                    usersCurrentActionMap.remove(chatId);
                    messageHelper.editAndSendMessage(chatId, "The details about the new forklift must be here.", messageId);
                    messageHelper.sendMessageWithKeyboard(chatId, "Forklift has been added successfully!", keyboardHelper.createStartKeyboard());
                } else {
                    messageHelper.prepareAndSendMessage(chatId, "Sorry, the status was not recognized");
                }
                break;
            default:
                messageHelper.prepareAndSendMessage(chatId, COMMAND_NOT_RECOGNIZED_MESSAGE);
                break;
        }
    }
}
