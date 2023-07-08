package com.java.model;

public class AssistantMessage {
    private MessageRole role;
    private String content;

    public AssistantMessage(MessageRole role, String content) {
        this.role = role;
        this.content = content;
    }

    public MessageRole getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }
}