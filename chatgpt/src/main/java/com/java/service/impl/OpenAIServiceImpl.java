package com.java.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.exception.ChatGPTException;
import com.java.model.AssistantMessage;
import com.java.model.MessageRole;
import com.java.service.OpenAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class OpenAIServiceImpl implements OpenAIService {
    @Value(("${api.url.completions}"))
    String url;
    @Value("${api.token}")
    String apiKey;
    @Value("${bot.text.model}")
    String model;

    // Create a RestTemplate instance
    private final RestTemplate restTemplate;

    private final Integer DELAY_BETWEEN_REQUESTS = 2000;

    @Autowired
    private RetryTemplate retryTemplate;

    private final List<AssistantMessage> sessionMessages = new ArrayList<>();

    public OpenAIServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String chatGPTRequestSessionBased(String message) {
        // Create the request body as a JSON string with all session messages
        AssistantMessage userMessage = new AssistantMessage(MessageRole.USER, message);
        sessionMessages.add(userMessage);
        String requestBody = buildSessionRequestBody();
        return sendMessageToChatGPT(requestBody);
    }

    @Override
    public CompletableFuture<String> chatGPTRequestMemoryLess(String message) {
        return CompletableFuture.supplyAsync(() -> {
            // Create the stateless request body as a JSON string
            String sanitizedMessage = sanitizeMessage(message);
            String requestBody =
                    String.format("{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}",
                            model, sanitizedMessage);
            return retryTemplate.execute(context -> sendMessageToChatGPT(requestBody));
        });
    }

    @Override
    public Double calculateMatchRateForCvAndJob(String message) {
        Double jobRate = null;
        try {
            // Create the stateless request body as a JSON string
            String sanitizedMessage = sanitizeMessage(message);
            String requestBody =
                    String.format("{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}",
                            model, sanitizedMessage);
            log.info("Message for ChatGPT was build");
            jobRate = Double.parseDouble(sendMessageToChatGPT(requestBody));
            Thread.sleep(DELAY_BETWEEN_REQUESTS);
        } catch (Exception e) {
            log.error("ChatGPT returned not a value for some reason", e);
        }
        return jobRate;
    }

    @Override
    public String simpleSessionLessRequest(String message) {
        // Create the stateless request body as a JSON string
        String sanitizedMessage = sanitizeMessage(message);
        String requestBody =
                String.format("{\"model\":\"%s\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}",
                        model, sanitizedMessage);
        log.info("Message for ChatGPT was build");
        return sendMessageToChatGPT(requestBody);
    }

    private String sendMessageToChatGPT(String requestBody) throws ChatGPTException {
        // Create headers with Content-Type and Authorization
        HttpHeaders headers = setHeaders();

        // Create the request entity with headers and body
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            log.info("Sending the POST request to ChatGPT");
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            // Check the response status
            if (response.getStatusCode() != HttpStatus.OK) {
                throw new ChatGPTException("ChatGPT request failed with status: " + response.getStatusCode());
            }

            log.info("Extracting chatGPT answer");
            return extractContentFromResponse(response);
        } catch (RestClientException e) {
            throw new ChatGPTException("Error communicating with ChatGPT: " + e.getMessage());
        }
    }


    private String buildSessionRequestBody() {
        List<String> messageStrings = new ArrayList<>();
        for (AssistantMessage message : sessionMessages) {
            String messageString = String.format("{\"role\":\"%s\",\"content\":\"%s\"}",
                    message.getRole().getValue(), message.getContent());
            messageStrings.add(messageString);
        }
        String messagesBody = String.join(",", messageStrings);
        return String.format("{\"model\":\"%s\",\"messages\":[%s]}", model, messagesBody);
    }

    private HttpHeaders setHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", apiKey);
        return headers;
    }

    private static String extractContentFromResponse(ResponseEntity<String> response) {
        try {
            // Create an ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();

            // Parse the response body as a JSON string
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            // Extract the content from the JSON structure
            JsonNode choicesNode = jsonNode.path("choices");
            if (choicesNode.isArray() && choicesNode.size() > 0) {
                JsonNode messageNode = choicesNode.get(0).path("message");
                if (messageNode.has("content")) {
                    return messageNode.get("content").asText();
                }
            }

            // Return null if content extraction failed
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String sanitizeMessage(String text) {
        return text.replaceAll("[^a-zA-Z0-9 ]", " ");
    }
}

