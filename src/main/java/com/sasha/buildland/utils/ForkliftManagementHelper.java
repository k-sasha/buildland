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

        log.info("All forklifts command received for chatId: {}", chatId);
    }


    public void findAllForkliftsByCapacityCommandReceived(long chatId) {
        usersCurrentActionMap.put(chatId, "find_all_forklifts_by_capacity");
        messageHelper.sendMessageWithKeyboard(chatId, "Find all forklifts by capacity", keyboardHelper.createReturnKeyboard());
        messageHelper.prepareAndSendMessage(chatId, "Enter the capacity of the forklifts you are looking for:");
        log.info("Initiated search for forklifts by capacity for chatId: {}", chatId);
    }

    public void findAllForkliftsByPriceCommandReceived(long chatId) {
        usersCurrentActionMap.put(chatId, "find_all_forklifts_by_price");
        messageHelper.sendMessageWithKeyboard(chatId, "Find all forklifts by price", keyboardHelper.createReturnKeyboard());
        messageHelper.prepareAndSendMessage(chatId, "Enter the price of the forklifts you are looking for:");
        log.info("Initiated search for forklifts by price for chatId: {}", chatId);
    }

    public void handleAddForkliftResponse(long chatId, String response) {
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
    }

    public void handleAddForkliftResponseWithInlineKeyboard(long chatId, String callBackData, long messageId) {
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
                    usersCurrentActionMap.put(chatId, "set_sale_price");
                    messageHelper.editAndSendMessage(chatId, "Please set the sale price:", messageId);
                    log.info("Status set to '{}' for chatId: {}", status, chatId);
                } else {
                    messageHelper.prepareAndSendMessage(chatId, "Sorry, the status was not recognized");
                    log.warn("Unrecognized status from callback data '{}' for chatId: {}", callBackData, chatId);
                }
                break;
            default:
                messageHelper.prepareAndSendMessage(chatId, COMMAND_NOT_RECOGNIZED_MESSAGE);
                log.warn("Unrecognized action for chatId: {}.", chatId);
                break;
        }
    }

    public void handleSearchForkliftByCapacityResponse(long chatId, String response) {
        int capacity;
        try {
            capacity = Integer.parseInt(response);
        } catch (NumberFormatException e) {
            messageHelper.prepareAndSendMessage(chatId, "Invalid capacity entered. Please enter a numeric value.");
            log.error("Invalid capacity format received from chatId {}: {}", chatId, response);
            return;
        }

        List<Forklift> matchingForklifts = forkliftService.findForkliftsByCapacity(capacity);

        if (matchingForklifts.isEmpty()) {
            messageHelper.prepareAndSendMessage(chatId, "No forklifts found matching the capacity: " + capacity + "lb.");
            log.info("No forklifts found with capacity {} for chatId {}", capacity, chatId);
        } else {
            StringBuilder responseText = new StringBuilder("Forklifts found with capacity " + capacity + "lb:\n\n");
            for (Forklift forklift : matchingForklifts) {
                responseText.append(forklift.formatForkliftInfo()).append("\n\n");
            }
            messageHelper.sendMessageWithKeyboard(chatId, responseText.toString(), keyboardHelper.createStartKeyboard());
            log.info("{} forklifts found with capacity {} for chatId {}", matchingForklifts.size(), capacity, chatId);
        }

        usersCurrentActionMap.put(chatId, "completed");

    }

    public void handleSearchForkliftByPriceResponse(long chatId, String response) {
        int price;
        try {
            price = Integer.parseInt(response);
        } catch (NumberFormatException e) {
            messageHelper.prepareAndSendMessage(chatId, "Invalid price entered. Please enter a numeric value.");
            log.error("Invalid price format received from chatId {}: {}", chatId, response);
            return;
        }

        List<Forklift> matchingForklifts = forkliftService.findForkliftsByPrice(price);

        if (matchingForklifts.isEmpty()) {
            messageHelper.prepareAndSendMessage(chatId, "No forklifts found matching the price: $" + price + ".");
            log.info("No forklifts found with price {} for chatId {}", price, chatId);
        } else {
            StringBuilder responseText = new StringBuilder("Forklifts found with price $" + price + ":\n\n");
            for (Forklift forklift : matchingForklifts) {
                responseText.append(forklift.formatForkliftInfo()).append("\n\n");
            }
            messageHelper.sendMessageWithKeyboard(chatId, responseText.toString(), keyboardHelper.createStartKeyboard());
            log.info("{} forklifts found with price {} for chatId {}", matchingForklifts.size(), price, chatId);
        }

        usersCurrentActionMap.put(chatId, "completed");

    }

}
