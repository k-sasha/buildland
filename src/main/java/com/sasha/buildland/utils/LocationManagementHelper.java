package com.sasha.buildland.utils;

import com.sasha.buildland.entity.Location;
import com.sasha.buildland.service.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class LocationManagementHelper {

    private final LocationService locationService;
    private final KeyboardHelper keyboardHelper;
    private final MessageHelper messageHelper;

    private Map<Long, Location> usersLocationMap = new HashMap<>();
    public Map<Long, String> usersCurrentActionMap = new HashMap<>();

    private static final String COMMAND_NOT_RECOGNIZED_MESSAGE = "Sorry, the command was not recognized";

    @Autowired
    public LocationManagementHelper(LocationService locationService,
                                    @Lazy KeyboardHelper keyboardHelper,
                                    @Lazy MessageHelper messageHelper) {
        this.locationService = locationService;
        this.keyboardHelper = keyboardHelper;
        this.messageHelper = messageHelper;
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

        location.setLocation(response);
        locationService.saveLocation(location);
        usersLocationMap.remove(chatId);
        usersCurrentActionMap.put(chatId, "completed");
        messageHelper.sendMessageWithKeyboard(chatId, "Location " + response + " has been added successfully!", keyboardHelper.createStartKeyboard());
        log.info("Location successfully saved for chatId: {}, location: {}", chatId, location);
    }

}
