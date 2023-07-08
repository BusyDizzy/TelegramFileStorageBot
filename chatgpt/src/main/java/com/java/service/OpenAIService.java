package com.java.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.java.model.AssistantMessage;
import com.java.model.MessageRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OpenAIService {
    @Value(("${api.url.completions}"))
    String url;
    @Value("${api.token}")
    String apiKey;
    @Value("${bot.text.model}")
    String model;

    private List<AssistantMessage> sessionMessages = new ArrayList<>();

    public String chatGPTRequest(String message) {

        AssistantMessage userMessage = new AssistantMessage(MessageRole.USER, message);
        sessionMessages.add(userMessage);

        // Create the request body as a JSON string with all session messages
        String requestBody = buildSessionRequestBody();

        // Create headers with Content-Type and Authorization
        HttpHeaders headers = setHeaders();

        // Create the request entity with headers and body
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        // Create a RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        log.info("Sending the POST request to ChatGPT");
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        // Check the response status
        if (response.getStatusCode() == HttpStatus.OK) {
            log.info("Extracting chatGPT answer");
            return extractContentFromResponse(response);
        } else {
            log.info("Request failed with status: {}", response.getStatusCode());
        }
        return "Sorry something went wrong...";
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
}
