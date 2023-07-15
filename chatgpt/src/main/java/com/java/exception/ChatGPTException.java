package com.java.exception;

public class ChatGPTException extends RuntimeException {
    public ChatGPTException(String message) {
        super(message);
    }
}