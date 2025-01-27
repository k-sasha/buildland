package com.sasha.buildland.utils;

import com.sasha.buildland.entity.InlineKeyboardObject;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


@Component
public class KeyboardHelper {

    private static final String BUTTON_SUFFIX = "_BUTTON";
    private Map<String, InlineKeyboardObject> buttonMap = new HashMap<>();
    private Map<String, String> confirmationButtonMap = new HashMap<>();
    private Map<String, String> enumButtonMap = new HashMap<>();

    public Map<String, InlineKeyboardObject> getButtonMap() {
        return buttonMap;
    }

    public Map<String, String> getConfirmationButtonMap() {
        return confirmationButtonMap;
    }

    public Map<String, String> getEnumButtonMap() {
        return enumButtonMap;
    }


    public ReplyKeyboardMarkup createStartKeyboard() {
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

    public ReplyKeyboardMarkup createAddKeyboard() {
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

    public ReplyKeyboardMarkup createGetKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        //create a list of rows
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        // first row of buttons
        KeyboardRow row = new KeyboardRow();
        row.add("get all forklifts");
        row.add("find by capacity");
        keyboardRows.add(row); // add the row to the list

        // second row of buttons
        row = new KeyboardRow();
        row.add("find by price");
        row.add("main menu");
        keyboardRows.add(row); // add the row to the list

        // add to the keyboard
        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;
    }

    public ReplyKeyboardMarkup createReturnKeyboard() {
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

    public InlineKeyboardMarkup createInlineKeyboard(List<String> list) {
        //  create keyboard with buttons from List
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>(); // create rows

        // For each list's element
        for (int i = 0; i < list.size(); i += 2) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();  // create one row

            var firstButton = new InlineKeyboardButton();  // create first button
            firstButton.setText(list.get(i));
            String button1 = list.get(i).toUpperCase() + BUTTON_SUFFIX;
            firstButton.setCallbackData(button1);
            enumButtonMap.put(button1, list.get(i));
            rowInline.add(firstButton);

            // Check if there is the next element to add into the same row
            if (i + 1 < list.size()) {
                var secondButton = new InlineKeyboardButton();
                secondButton.setText(list.get(i + 1));
                String button2 = list.get(i + 1).toUpperCase() + BUTTON_SUFFIX;
                secondButton.setCallbackData(button2);
                enumButtonMap.put(button2, list.get(i + 1));
                rowInline.add(secondButton);
            }

            rowsInline.add(rowInline); // add row to rows
        }

        markupInline.setKeyboard(rowsInline); //add to the keyboard

        return markupInline;
    }

    public InlineKeyboardMarkup createInlineKeyboardConfirmation() {
        //  create keyboard with buttons from List
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>(); // create rows

        List<InlineKeyboardButton> rowInline = new ArrayList<>();  // create one row

        var yesButton = new InlineKeyboardButton();
        yesButton.setText("Yes");
        yesButton.setCallbackData("YES_BUTTON");
        confirmationButtonMap.put("YES_BUTTON", "Yes");

        var noButton = new InlineKeyboardButton();
        noButton.setText("No");
        noButton.setCallbackData("NO_BUTTON");
        confirmationButtonMap.put("NO_BUTTON", "No");

        rowInline.add(yesButton); // add to the row
        rowInline.add(noButton);

        rowsInline.add(rowInline); // add row to rows

        markupInline.setKeyboard(rowsInline); //add to the keyboard

        return markupInline;
}

    public InlineKeyboardMarkup createInlineKeyboard2(List<? extends InlineKeyboardObject> objectNames) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        for (int i = 0; i < objectNames.size(); i += 2) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();

            InlineKeyboardObject firstObjectName = objectNames.get(i);
            var firstButton = new InlineKeyboardButton();
            firstButton.setText(firstObjectName.getName());
            String button1 = firstObjectName.getName() + BUTTON_SUFFIX;
            firstButton.setCallbackData(button1);
            buttonMap.put(button1, firstObjectName);
            rowInline.add(firstButton);

            if (i + 1 < objectNames.size()) {
                InlineKeyboardObject secondObjectName = objectNames.get(i + 1);
                var secondButton = new InlineKeyboardButton();
                secondButton.setText(secondObjectName.getName());
                String button2 = secondObjectName.getName() + BUTTON_SUFFIX;
                secondButton.setCallbackData(button2);
                buttonMap.put(button2, secondObjectName);
                rowInline.add(secondButton);
            }

            rowsInline.add(rowInline);
        }

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }


    public ReplyKeyboardMarkup createDeleteKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        //create a list of rows
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        // first row of buttons
        KeyboardRow row = new KeyboardRow();
        row.add("delete forklift");
        row.add("delete location");
        keyboardRows.add(row); // add the row to the list

        // second row of buttons
        row = new KeyboardRow();
        row.add("delete manufacturer");
        row.add("main menu");
        keyboardRows.add(row); // add the row to the list

        // add to the keyboard
        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;
    }
}
