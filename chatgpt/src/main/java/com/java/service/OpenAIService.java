package com.java.service;

public interface OpenAIService {
    String chatGPTRequestSessionBased(String message);

    String chatGPTRequestMemoryLess(String message);
}
