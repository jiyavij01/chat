package com.ai.chat.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ai.chat.models.ChatMessage;

@Service
public class SarvamAiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public String askGemini(List<ChatMessage> history, String userMessage) {

        String url =
                "https://generativelanguage.googleapis.com/v1beta/models/"
                        + model
                        + ":generateContent?key="
                        + apiKey;

        List<Map<String, Object>> contents = new ArrayList<>();

        // Previous chat history
        for (ChatMessage msg : history) {

            String role =
                    msg.getRole().equalsIgnoreCase("assistant")
                            ? "model"
                            : "user";

            Map<String, Object> part = new HashMap<>();
            part.put("text", msg.getContent());

            List<Map<String, Object>> parts = new ArrayList<>();
            parts.add(part);

            Map<String, Object> content = new HashMap<>();
            content.put("role", role);
            content.put("parts", parts);

            contents.add(content);
        }

        // Current user message
        Map<String, Object> currentPart = new HashMap<>();
        currentPart.put("text", userMessage);

        List<Map<String, Object>> currentParts = new ArrayList<>();
        currentParts.add(currentPart);

        Map<String, Object> currentContent = new HashMap<>();
        currentContent.put("role", "user");
        currentContent.put("parts", currentParts);

        contents.add(currentContent);

        // Request body
        Map<String, Object> body = new HashMap<>();
        body.put("contents", contents);

        // Optional generation config
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", 0.2);
        generationConfig.put("maxOutputTokens", 1000);

        body.put("generationConfig", generationConfig);

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Request entity
        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        try {

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            url,
                            entity,
                            Map.class
                    );

            // Debugging
            System.out.println(response.getBody());

            // Parse Gemini response
            List candidates =
                    (List) response.getBody().get("candidates");

            if (candidates == null || candidates.isEmpty()) {
                return "No response from Gemini.";
            }

            Map firstCandidate = (Map) candidates.get(0);

            Map content =
                    (Map) firstCandidate.get("content");

            List parts =
                    (List) content.get("parts");

            Map firstPart = (Map) parts.get(0);

            return firstPart.get("text").toString();

        } catch (Exception e) {

            e.printStackTrace();

            return "Error: " + e.getMessage();
        }
    }
}