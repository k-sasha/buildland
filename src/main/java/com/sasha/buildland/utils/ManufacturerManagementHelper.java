package com.sasha.buildland.utils;

import com.sasha.buildland.entity.InlineKeyboardObject;

import com.sasha.buildland.entity.Manufacturer;
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
public class ManufacturerManagementHelper {

    private final ManufacturerService manufacturerService;
    private final KeyboardHelper keyboardHelper;
    private final MessageHelper messageHelper;

    private Map<Long, Manufacturer> usersManufacturerMap = new HashMap<>();
    private Map<Long, String> usersCurrentActionMap = new HashMap<>();

    private static final String COMMAND_NOT_RECOGNIZED_MESSAGE = "Sorry, the command was not recognized";
    private static final String COMPLETED = "completed";

    @Autowired
    public ManufacturerManagementHelper(ManufacturerService manufacturerService,
                                        @Lazy KeyboardHelper keyboardHelper,
                                        @Lazy MessageHelper messageHelper) {
        this.manufacturerService = manufacturerService;
        this.keyboardHelper = keyboardHelper;
        this.messageHelper = messageHelper;
    }

    public Map<Long, String> getUsersCurrentActionMap() {
        return usersCurrentActionMap;
    }

    public void addManufacturerCommandReceived(long chatId) {

        usersManufacturerMap.put(chatId, new Manufacturer());
        usersCurrentActionMap.put(chatId, "set_manufacturer");
        messageHelper.sendMessageWithKeyboard(chatId, "New manufacturer", keyboardHelper.createReturnKeyboard());
        messageHelper.prepareAndSendMessage(chatId, "Please set new manufacturer:");
        log.info("Initiated manufacturer addition process for chatId: {}", chatId);
    }

    public void handleUserResponse(long chatId, String response) {
        Manufacturer manufacturer = usersManufacturerMap.get(chatId);
        if (manufacturer == null) {
            messageHelper.prepareAndSendMessage(chatId, COMMAND_NOT_RECOGNIZED_MESSAGE);
            log.warn("Manufacturer not found for chatId: {}", chatId);
            return;
        }

        manufacturer.setManufacturerName(response);
        manufacturerService.saveManufacturer(manufacturer);
        usersManufacturerMap.remove(chatId);
        usersCurrentActionMap.put(chatId, COMPLETED);
        messageHelper.sendMessageWithKeyboard(chatId, "Manufacturer " + response + " has been added successfully!", keyboardHelper.createStartKeyboard());
        log.info("Manufacturer successfully saved for chatId: {}, manufacturer: {}", chatId, manufacturer);
    }

    public void deleteManufacturerCommandReceived(long chatId) {

        usersCurrentActionMap.put(chatId, "delete_manufacturer");
        messageHelper.sendMessageWithKeyboard(chatId, "Delete manufacturer", keyboardHelper.createReturnKeyboard());
        String text = "Please select a Manufacturer to delete:";
        List<Manufacturer> manufacturers = manufacturerService.getAllManufacturers();
        messageHelper.sendMessageWithInlineKeyboard2(manufacturers, text, chatId);
        log.info("Initiated manufacturer deleting process for chatId: {}", chatId);
    }

    public void handleUserResponseWithInlineKeyboard(long chatId, String callBackData, long messageId) {
        switch (usersCurrentActionMap.get(chatId)) {
            case "delete_manufacturer": {
                InlineKeyboardObject manufacturer = keyboardHelper.getButtonMap().get(callBackData);
                if (manufacturer != null) {
                    usersManufacturerMap.put(chatId, (Manufacturer) manufacturer);
                    String manufacturerName = manufacturer.getName();
                    usersCurrentActionMap.put(chatId, "deletion_confirmation");
                    messageHelper.sendMessageConfirmationWithInlineKeyboard("Do you really want to delete the " + manufacturerName + " manufacturer", chatId, messageId);
                    log.info("Deletion request for manufacturer: {} by chatId: {}", manufacturerName, chatId);
                } else {
                    messageHelper.prepareAndSendMessage(chatId, COMMAND_NOT_RECOGNIZED_MESSAGE);
                    log.warn("Command not recognized for chatId: {}", chatId);
                }
                break;
            }
            case "deletion_confirmation": {
                String confirmation = keyboardHelper.getConfirmationButtonMap().get(callBackData);
                System.out.println(confirmation);

                Manufacturer manufacturer = usersManufacturerMap.get(chatId);
                Long manufacturerId = manufacturer.getId();
                String manufacturerName = manufacturer.getName();
                if (confirmation.equals("Yes")) {
                    manufacturerService.deleteManufacturer(manufacturerId);
                    usersManufacturerMap.remove(chatId);
                    usersCurrentActionMap.put(chatId, COMPLETED);
                    messageHelper.editAndSendMessage(chatId, "Deleting the manufacturer " + manufacturerName, messageId);
                    messageHelper.sendMessageWithKeyboard(chatId, "Manufacturer " + manufacturerName + " has been deleted successfully!", keyboardHelper.createStartKeyboard());
                    log.info("Manufacturer {} deleted for chatId: {}", manufacturerName, chatId);
                } else if (confirmation.equals("No")) {
                    usersManufacturerMap.remove(chatId);
                    usersCurrentActionMap.put(chatId, COMPLETED);
                    messageHelper.editAndSendMessage(chatId, "Manufacturer deletion canceled", messageId);
                    messageHelper.sendMessageWithKeyboard(chatId, "Deletion of " + manufacturerName + " manufacturer has been canceled!", keyboardHelper.createStartKeyboard());
                    log.info("Manufacturer deletion canceled for chatId: {}", chatId);
                }
                break;
            }
            default:
                messageHelper.prepareAndSendMessage(chatId, COMMAND_NOT_RECOGNIZED_MESSAGE);
                log.warn("Unrecognized action for chatId: {}.", chatId);
                break;
        }
    }
}
