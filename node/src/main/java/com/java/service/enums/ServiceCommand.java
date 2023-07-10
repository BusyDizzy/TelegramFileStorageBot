package com.java.service.enums;

public enum ServiceCommand {
    HELP("/help"),
    REGISTRATION("/registration"),
    CANCEL("/cancel"),
    START("/start"),

    SHOW_JOBS("/show_jobs"),

    DOWNLOAD_JOBS("/download_jobs"),

    MATCH_JOBS("/match_jobs"),
    GENERATE_AND_SEND("/generate_and_send"),
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
}