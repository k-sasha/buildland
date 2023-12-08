package com.sasha.buildland.utils;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class KeyboardHelper {

    private static final String BUTTON_SUFFIX = "_BUTTON";

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
        for (int i = 0; i < list.size(); i+=2) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();  // create one row

            var firstButton = new InlineKeyboardButton();  // create first button
            firstButton.setText(list.get(i));
            firstButton.setCallbackData(list.get(i).toUpperCase() + BUTTON_SUFFIX); // indicator (id), that allow bot to understand which button is pressed
            rowInline.add(firstButton);

            // Check if there is the next element to add into the same row
            if (i + 1 < list.size()) {
                var secondButton = new InlineKeyboardButton();
                secondButton.setText(list.get(i + 1));
                secondButton.setCallbackData(list.get(i + 1).toUpperCase() + BUTTON_SUFFIX);
                rowInline.add(secondButton);
            }

            rowsInline.add(rowInline); // add row to rows
        }

        markupInline.setKeyboard(rowsInline); //add to the keyboard

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
