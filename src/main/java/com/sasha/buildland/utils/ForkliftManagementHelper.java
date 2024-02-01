package com.sasha.buildland.utils;

import com.sasha.buildland.entity.*;
import com.sasha.buildland.enums.Status;
import com.sasha.buildland.service.ForkliftService;
import com.sasha.buildland.service.ForkliftTechnicalDetailsService;
import com.sasha.buildland.service.LocationService;
import com.sasha.buildland.service.ManufacturerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ForkliftManagementHelper {

    private final ForkliftService forkliftService;
    private final ForkliftTechnicalDetailsService technicalDetailsService;
    private final ManufacturerService manufacturerService;
    private final LocationService locationService;
    private final KeyboardHelper keyboardHelper;
    private final MessageHelper messageHelper;

    private Map<Long, Forklift> usersForkliftMap = new HashMap<>();
    private Map<Long, String> usersCurrentActionMap = new HashMap<>();

    private static final String COMMAND_NOT_RECOGNIZED_MESSAGE = "Sorry, the command was not recognized";

    @Autowired
    public ForkliftManagementHelper(ForkliftService forkliftService,
                                    ForkliftTechnicalDetailsService technicalDetailsService,
                                    ManufacturerService manufacturerService,
                                    LocationService locationService,
                                    @Lazy KeyboardHelper keyboardHelper,
                                    @Lazy MessageHelper messageHelper) {
        this.forkliftService = forkliftService;
        this.technicalDetailsService = technicalDetailsService;
        this.manufacturerService = manufacturerService;
        this.locationService = locationService;
        this.keyboardHelper = keyboardHelper;
        this.messageHelper = messageHelper;
    }

    public Map<Long, String> getUsersCurrentActionMap() {
        return usersCurrentActionMap;
    }

    public void addForkliftCommandReceived(long chatId) {

        usersForkliftMap.put(chatId, new Forklift());
        usersCurrentActionMap.put(chatId, "set_inventory_number");
        messageHelper.sendMessageWithKeyboard(chatId, "New forklift", keyboardHelper.createReturnKeyboard());
        //TODO show previous inventory number if exists
        messageHelper.prepareAndSendMessage(chatId, "Please set inventory number:");
        log.info("Initiated forklift addition process for chatId: {}", chatId);
    }

    public void getAllForkliftsCommandReceived(long chatId) {
        usersCurrentActionMap.put(chatId, "get_forklift");
        messageHelper.sendMessageWithKeyboard(chatId, "Get all forklifts", keyboardHelper.createReturnKeyboard());

        List<Forklift> forklifts = forkliftService.getAllForklifts();

        // Generate display text for each forklift
        StringBuilder allForkliftsText = new StringBuilder("Available Forklifts:\n");
        for (Forklift forklift : forklifts) {
            allForkliftsText.append(forklift.formatForkliftInfo()).append("\n\n");
        }

        usersCurrentActionMap.put(chatId, "completed");
        messageHelper.sendMessageWithKeyboard(chatId, allForkliftsText.toString(), keyboardHelper.createStartKeyboard());
        log.info("Initiated forklift selection process for chatId: {}", chatId);
    }

    public void handleUserResponse(long chatId, String response) {
        Forklift forklift = usersForkliftMap.get(chatId);
        if (forklift == null) {
            messageHelper.prepareAndSendMessage(chatId, COMMAND_NOT_RECOGNIZED_MESSAGE);
            log.warn("Forklift not found for chatId: {}", chatId);
            return;
        }

        String text;

        switch (usersCurrentActionMap.get(chatId)) {
            case "set_inventory_number":
                forklift.setNumber(response);
                usersCurrentActionMap.put(chatId, "set_manufacturer");
                text = "Please set the manufacturer:";
                List<Manufacturer> manufacturers = manufacturerService.getAllManufacturers();
                messageHelper.sendMessageWithInlineKeyboard2(manufacturers, text, chatId);
                log.info("User response for chatId {}: set inventory_number to {}", chatId, response);
                break;
            case "set_model":
                forklift.setModel(response);
                usersCurrentActionMap.put(chatId, "set_serial_number");
                text = "Please set the serial number:";
                messageHelper.prepareAndSendMessage(chatId, text);
                log.info("User response for chatId {}: set model to {}", chatId, response);
                break;
            case "set_serial_number":
                forklift.setSerial(response);
                usersCurrentActionMap.put(chatId, "set_load_capacity");
                text = "Please set the load capacity:";
                messageHelper.prepareAndSendMessage(chatId, text);
                log.info("User response for chatId {}: set serial_number to {}", chatId, response);
                break;
            case "set_load_capacity":
                try {
                    int capacity = Integer.parseInt(response);
                    ForkliftTechnicalDetails forkliftTechnicalDetails = new ForkliftTechnicalDetails();
                    forkliftTechnicalDetails.setLoadCapacity(capacity);
                    technicalDetailsService.saveForkliftTechnicalDetails(forkliftTechnicalDetails);

                    forklift.setTechnicalDetails(forkliftTechnicalDetails);
                    usersCurrentActionMap.put(chatId, "set_location");
                    text = "Please set the location:";
                    List<Location> locations = locationService.getAllLocations();
                    messageHelper.sendMessageWithInlineKeyboard2(locations, text, chatId);
                    log.info("User response for chatId {}: set load_capacity to {}", chatId, response);
                } catch (NumberFormatException e) {
                    messageHelper.prepareAndSendMessage(chatId, "Invalid load capacity. Please enter a valid number.");
                    log.error("NumberFormatException for chatId {}: {}", chatId, e.getMessage());
                }
                break;
            case "set_sale_price":
                try {
                    int price = Integer.parseInt(response);
                    forklift.setPrice(price);
                    forkliftService.saveForklift(forklift);
                    usersForkliftMap.remove(chatId);
                    usersCurrentActionMap.put(chatId, "completed");
                    messageHelper.sendMessageWithKeyboard(chatId, "Forklift has been added successfully!", keyboardHelper.createStartKeyboard());

                    log.info("User response for chatId {}: set price to {}", chatId, response);
                } catch (NumberFormatException e) {
                    messageHelper.prepareAndSendMessage(chatId, "Invalid price. Please enter a valid number.");
                    log.error("NumberFormatException for chatId {}: {}", chatId, e.getMessage());
                }
                break;

            default:
                messageHelper.prepareAndSendMessage(chatId, "Sorry, I didn't understand that.");
                log.warn("Unrecognized action for chatId: {}. User response: '{}'", chatId, response);
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

    public void handleUserResponseWithInlineKeyboard(long chatId, String callBackData, long messageId) {
        Forklift forklift = usersForkliftMap.get(chatId);
        if (forklift == null) {
            messageHelper.prepareAndSendMessage(chatId, COMMAND_NOT_RECOGNIZED_MESSAGE);
            log.warn("Forklift not found for chatId: {}", chatId);
            return;
        }

        switch (usersCurrentActionMap.get(chatId)) {
            case "set_manufacturer":
                InlineKeyboardObject manufacturer = keyboardHelper.getButtonMap().get(callBackData);
                if (manufacturer != null) {
                    forklift.setManufacturer((Manufacturer) manufacturer);
                    usersCurrentActionMap.put(chatId, "set_model");
                    messageHelper.editAndSendMessage(chatId, "Please set the model:", messageId);
                    log.info("Manufacturer set to '{}' for chatId: {}", manufacturer, chatId);
                } else {
                    messageHelper.prepareAndSendMessage(chatId, COMMAND_NOT_RECOGNIZED_MESSAGE);
                    log.warn("Command not recognized for chatId: {}", chatId);
                }
                break;
            case "set_location":
                InlineKeyboardObject location = keyboardHelper.getButtonMap().get(callBackData);
                if (location != null) {
                    forklift.setLocation((Location) location);
                    usersCurrentActionMap.put(chatId, "set_forklift_status");

                    String text = "Please set the forklift status:";
                    List<String> statusList = Status.getDisplayNames();
                    messageHelper.sendMessageWithInlineKeyboard(statusList, text, chatId, messageId);

                    log.info("Location set to '{}' for chatId: {}", location, chatId);
                } else {
                    messageHelper.prepareAndSendMessage(chatId, COMMAND_NOT_RECOGNIZED_MESSAGE);
                    log.warn("Command not recognized for chatId: {}", chatId);
                }
                break;
            case "set_forklift_status":
                String statusDisplayName = keyboardHelper.getEnumButtonMap().get(callBackData);
                if (statusDisplayName != null) {
                    Status status = Status.fromDisplayName(statusDisplayName);
                    forklift.setStatus(status);

//                    usersCurrentActionMap.put(chatId, "set_technical_details");
//                    messageHelper.editAndSendMessage(chatId, "Please set the technical details:", messageId);
                    usersCurrentActionMap.put(chatId, "set_sale_price");
                    messageHelper.editAndSendMessage(chatId, "Please set the sale price:", messageId);
                    log.info("Status set to '{}' for chatId: {}", status, chatId);
                } else {
                    messageHelper.prepareAndSendMessage(chatId, "Sorry, the status was not recognized");
                    log.warn("Unrecognized status from callback data '{}' for chatId: {}", callBackData, chatId);
                }

//                 FINISH
//                forkliftService.saveForklift(forklift);
//                usersForkliftMap.remove(chatId);
//                usersCurrentActionMap.put(chatId, "completed");
//                messageHelper.sendMessageWithKeyboard(chatId, "Forklift has been added successfully!", keyboardHelper.createStartKeyboard());

                break;
            default:
                messageHelper.prepareAndSendMessage(chatId, COMMAND_NOT_RECOGNIZED_MESSAGE);
                log.warn("Unrecognized action for chatId: {}.", chatId);
                break;
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
