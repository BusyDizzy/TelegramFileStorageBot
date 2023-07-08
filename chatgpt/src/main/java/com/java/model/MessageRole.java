package com.java.model;

public enum MessageRole {
    USER("user"),
    ASSISTANT("assistant");

    private final String value;

    MessageRole(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public String getValue() {
        return value;
    }
}