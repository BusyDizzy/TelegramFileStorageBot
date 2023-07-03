package com.java.service.enums;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public enum ServiceCommand {
    HELP("/help"),
    REGISTRATION("/registration"),
    CANCEL("/cancel"),
    START("/start"),
    MENU("/menu");
    private final String value;

    ServiceCommand(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static ServiceCommand fromValue(String v) {
        for (ServiceCommand c : ServiceCommand.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        return null;
    }

    public String getValue() {
        return value;
    }

    public static String getMenuText() {
        StringBuilder menuText = new StringBuilder("Available commands:\n");
        for (ServiceCommand command : ServiceCommand.values()) {
            menuText.append(command.getValue()).append("\n");
        }
        return menuText.toString();
    }

    public static InlineKeyboardMarkup getMenuMarkup() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        for (ServiceCommand command : ServiceCommand.values()) {
            if (command != MENU) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(command.getValue());
                button.setCallbackData(command.getValue());
                row.add(button);
            }
        }
        rows.add(row);

        markup.setKeyboard(rows);

        return markup;
    }
}