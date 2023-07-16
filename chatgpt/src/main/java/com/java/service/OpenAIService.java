package com.java.service;

import java.util.concurrent.CompletableFuture;

public interface OpenAIService {
    String chatGPTRequestSessionBased(String message);

    CompletableFuture<String> chatGPTRequestMemoryLess(String message);

    Double calculateMatchRateForCvAndJob(String message);

    String simpleSessionLessRequest(String message);
}
