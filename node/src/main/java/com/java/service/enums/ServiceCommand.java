package com.java.service.enums;

public enum ServiceCommand {

    START("/start"),

    REGISTRATION("/registration"),

    UPLOAD_CV("/upload_resume"),

    DOWNLOAD_JOBS("/download"),

    SHOW_DOWNLOADED("/show_downloaded"),

    MATCH("/match"),

    SHOW_MATCHED("/show_matched"),

    GENERATE_AND_SEND("/generate"),
    HELP("/help"),
    CANCEL("/cancel"),

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