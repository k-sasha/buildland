package com.sasha.buildland.utils;

import com.sasha.buildland.entity.Forklift;
import com.sasha.buildland.service.ForkliftService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
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

    private static final String COMMAND_NOT_RECOGNIZED_MESSAGE = "Sorry, the command was not recognized";

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

    public Map<Long, String> getUsersCurrentActionMap() {
        return usersCurrentActionMap;
    }

    public void addForkliftCommandReceived(long chatId) {

        usersForkliftMap.put(chatId, new Forklift());
        usersCurrentActionMap.put(chatId, "set_manufacturer");
        String text = "Please set the manufacturer:";
        messageHelper.sendMessageWithKeyboard(chatId, "New forklift", keyboardHelper.createReturnKeyboard());
        messageHelper.sendMessageWithInlineKeyboard(manufacturers, text, chatId);
        log.info("Initiated forklift addition process for chatId: {}", chatId);
    }

    public void handleUserResponse(long chatId, String response) {
        Forklift forklift = usersForkliftMap.get(chatId);
        if (forklift == null) {
            messageHelper.prepareAndSendMessage(chatId, COMMAND_NOT_RECOGNIZED_MESSAGE);
            log.warn("Forklift not found for chatId: {}", chatId);
            return;
        }

//        switch (usersCurrentActionMap.get(chatId)) {
//            case "set_model":
//                forklift.setModel(response);
//                usersCurrentActionMap.put(chatId, "set_capacity");
//                messageHelper.prepareAndSendMessage(chatId, "Please set the capacity:");
//                log.info("User response for chatId {}: set model to {}", chatId, response);
//                break;
//            case "set_capacity":
//                try {
//                    int capacity = Integer.parseInt(response);
//                    forklift.setCapacity(capacity);
//                    usersCurrentActionMap.put(chatId, "set_year");
//                    messageHelper.prepareAndSendMessage(chatId, "Please set the year:");
//                    log.info("User response for chatId {}: set capacity to {}", chatId, response);
//                } catch (NumberFormatException e) {
//                    messageHelper.prepareAndSendMessage(chatId, "Invalid capacity. Please enter a valid number.");
//                    log.error("NumberFormatException for chatId {}: {}", chatId, e.getMessage());
//                }
//                break;
//            case "set_year":
//                try {
//                    int year = Integer.parseInt(response);
//                    forklift.setYear(year);
//                    usersCurrentActionMap.put(chatId, "set_hours");
//                    messageHelper.prepareAndSendMessage(chatId, "Please set the hours:");
//                    log.info("User response for chatId {}: set year to {}", chatId, response);
//                } catch (NumberFormatException e) {
//                    messageHelper.prepareAndSendMessage(chatId, "Invalid year. Please enter a valid number.");
//                    log.error("NumberFormatException for chatId {}: {}", chatId, e.getMessage());
//                }
//                break;
//            case "set_hours":
//                try {
//                    Long hours = Long.parseLong(response);
//                    forklift.setHours(hours);
//                    usersCurrentActionMap.put(chatId, "set_location");
//                    String text = "Please set the location:";
//                    messageHelper.sendMessageWithInlineKeyboard(locations, text, chatId);
//                    log.info("User response for chatId {}: set hours to {}", chatId, response);
//                } catch (NumberFormatException e) {
//                    messageHelper.prepareAndSendMessage(chatId, "Invalid hours. Please enter a valid number.");
//                    log.error("NumberFormatException for chatId {}: {}", chatId, e.getMessage());
//                }
//                break;
//            default:
//                messageHelper.prepareAndSendMessage(chatId, "Sorry, I didn't understand that. Please click on the button");
//                log.warn("Unrecognized action for chatId: {}. User response: '{}'", chatId, response);
//        }
    }

    public void handleUserResponseWithInlineKeyboard(long chatId, String callBackData, long messageId ) {
        Forklift forklift = usersForkliftMap.get(chatId);
        if (forklift == null) {
            messageHelper.prepareAndSendMessage(chatId, COMMAND_NOT_RECOGNIZED_MESSAGE);
            log.warn("Forklift not found for chatId: {}", chatId);
            return;
        }

//        switch (usersCurrentActionMap.get(chatId)) {
//            case "set_manufacturer":
//                String manufacturer = buttonToManufacturerMap.get(callBackData);
//                if (manufacturer != null) {
//                    forklift.setManufacturer(manufacturer);
//                    usersCurrentActionMap.put(chatId, "set_model");
//                    messageHelper.editAndSendMessage(chatId, "Please set the model:", messageId);
//                    log.info("Manufacturer set to '{}' for chatId: {}", manufacturer, chatId);
//                } else {
//                    messageHelper.prepareAndSendMessage(chatId, "Sorry, the manufacturer was not recognized");
//                    log.warn("Unrecognized manufacturer from callback data '{}' for chatId: {}", callBackData, chatId);
//                }
//                break;
//            case "set_location":
//                String location = buttonToLocationMap.get(callBackData);
//                if (location != null) {
//                    forklift.setLocation(location);
//                    usersCurrentActionMap.put(chatId, "set_status");
//                    messageHelper.sendMessageWithInlineKeyboard(statuses, "Please set the status:", chatId, messageId);
//                    log.info("Location set to '{}' for chatId: {}", location, chatId);
//                } else {
//                    messageHelper.prepareAndSendMessage(chatId, "Sorry, the location was not recognized");
//                    log.warn("Unrecognized manufacturer from callback data '{}' for chatId: {}", callBackData, chatId);
//                }
//                break;
//            case "set_status":
//                String status = buttonToStatusMap.get(callBackData);
//                if (status != null) {
//                    forklift.setStatus(status);
//                    forkliftService.saveForklift(forklift);
//                    usersForkliftMap.remove(chatId);
//                    usersCurrentActionMap.put(chatId, "completed");
//                    messageHelper.editAndSendMessage(chatId, "The details about the new forklift must be here.", messageId);
//                    messageHelper.sendMessageWithKeyboard(chatId, "Forklift has been added successfully!", keyboardHelper.createStartKeyboard());
//                    log.info("Forklift successfully saved for chatId: {}, forklift: {}", chatId, forklift);
//                } else {
//                    messageHelper.prepareAndSendMessage(chatId, "Sorry, the status was not recognized");
//                    log.warn("Unrecognized manufacturer from callback data '{}' for chatId: {}", callBackData, chatId);
//                }
//                break;
//            default:
//                messageHelper.prepareAndSendMessage(chatId, COMMAND_NOT_RECOGNIZED_MESSAGE);
//                log.warn("Unrecognized action for chatId: {}.", chatId);
//                break;
//        }
    }
}
