package com.sasha.buildland.utils;

import com.sasha.buildland.entity.InlineKeyboardObject;
import com.sasha.buildland.entity.Location;
import com.sasha.buildland.service.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class LocationManagementHelper {

    private final LocationService locationService;
    private final KeyboardHelper keyboardHelper;
    private final MessageHelper messageHelper;

    private Map<Long, Location> usersLocationMap = new HashMap<>();
    private Map<Long, String> usersCurrentActionMap = new HashMap<>();

    private static final String COMMAND_NOT_RECOGNIZED_MESSAGE = "Sorry, the command was not recognized";
    private static final String COMPLETED = "completed";

    @Autowired
    public LocationManagementHelper(LocationService locationService,
                                    @Lazy KeyboardHelper keyboardHelper,
                                    @Lazy MessageHelper messageHelper) {
        this.locationService = locationService;
        this.keyboardHelper = keyboardHelper;
        this.messageHelper = messageHelper;
    }

    public Map<Long, String> getUsersCurrentActionMap() {
        return usersCurrentActionMap;
    }

    public void addLocationCommandReceived(long chatId) {

        usersLocationMap.put(chatId, new Location());
        usersCurrentActionMap.put(chatId, "set_location");
        messageHelper.sendMessageWithKeyboard(chatId, "New location", keyboardHelper.createReturnKeyboard());
        messageHelper.prepareAndSendMessage(chatId, "Please set new location:");
        log.info("Initiated location addition process for chatId: {}", chatId);
    }

    public void handleUserResponse(long chatId, String response) {
        Location location = usersLocationMap.get(chatId);
        if (location == null) {
            messageHelper.prepareAndSendMessage(chatId, COMMAND_NOT_RECOGNIZED_MESSAGE);
            log.warn("Location not found for chatId: {}", chatId);
            return;
        }

        location.setLocationName(response);
        locationService.saveLocation(location);
        usersLocationMap.remove(chatId);
        usersCurrentActionMap.put(chatId, COMPLETED);
        messageHelper.sendMessageWithKeyboard(chatId, "Location " + response + " has been added successfully!", keyboardHelper.createStartKeyboard());
        log.info("Location successfully saved for chatId: {}, location: {}", chatId, location);
    }

    public void deleteLocationCommandReceived(long chatId) {

        usersCurrentActionMap.put(chatId, "delete_location");
        messageHelper.sendMessageWithKeyboard(chatId, "Delete location", keyboardHelper.createReturnKeyboard());
        String text = "Please select a location to delete:";
        List<Location> locations = locationService.getAllLocations();
        messageHelper.sendMessageWithInlineKeyboard2(locations, text, chatId);
        log.info("Initiated location deleting process for chatId: {}", chatId);
    }

    public void handleUserResponseWithInlineKeyboard(long chatId, String callBackData, long messageId) {
        switch (usersCurrentActionMap.get(chatId)) {
            case "delete_location": {
                InlineKeyboardObject location = keyboardHelper.getButtonMap().get(callBackData);
                if (location != null) {
                    usersLocationMap.put(chatId, (Location) location);
                    String locationName = location.getName();
                    usersCurrentActionMap.put(chatId, "deletion_confirmation");
                    messageHelper.sendMessageConfirmationWithInlineKeyboard("Do you really want to delete the " + locationName + " location", chatId, messageId);
                    log.info("Deletion request for location: {} by chatId: {}", locationName, chatId);
                } else {
                    messageHelper.prepareAndSendMessage(chatId, "Sorry, the command was not recognized");
                    log.warn("Command not recognized for chatId: {}", chatId);
                }
                break;
            }
            case "deletion_confirmation": {
                String confirmation = keyboardHelper.getConfirmationButtonMap().get(callBackData);
                System.out.println(confirmation);

                Location location = usersLocationMap.get(chatId);
                Long locationId = location.getId();
                String locationName = location.getName();
                if (confirmation.equals("Yes")) {
                    locationService.deleteLocation(locationId);
                    usersLocationMap.remove(chatId);
                    usersCurrentActionMap.put(chatId, COMPLETED);
                    messageHelper.editAndSendMessage(chatId, "Deleting the location " + locationName, messageId);
                    messageHelper.sendMessageWithKeyboard(chatId, "Location " + locationName + " has been deleted successfully!", keyboardHelper.createStartKeyboard());
                    log.info("Location {} deleted for chatId: {}", locationName, chatId);
                } else if (confirmation.equals("No")) {
                    usersLocationMap.remove(chatId);
                    usersCurrentActionMap.put(chatId, COMPLETED);
                    messageHelper.editAndSendMessage(chatId, "Location deletion canceled", messageId);
                    messageHelper.sendMessageWithKeyboard(chatId, "Deletion of " + locationName + " location has been canceled!", keyboardHelper.createStartKeyboard());
                    log.info("Location deletion canceled for chatId: {}", chatId);
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
