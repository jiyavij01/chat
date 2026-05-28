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

	public String askSarvam(List<ChatMessage> history, String userMessage) {

	    String url =
	            "https://generativelanguage.googleapis.com/v1beta/models/"
	                    + model
	                    + ":generateContent?key="
	                    + apiKey;

	    List<Map<String, Object>> contents = new ArrayList<>();

	    // old chat history
	    for (ChatMessage msg : history) {

	        Map<String, Object> message = new HashMap<>();

	        message.put(
	                "role",
	                msg.getRole().equals("assistant")
	                        ? "model"
	                        : "user"
	        );

	        message.put(
	                "parts",
	                List.of(
	                        Map.of(
	                                "text",
	                                msg.getContent()
	                        )
	                )
	        );

	        contents.add(message);
	    }

	    // current user message
	    contents.add(
	            Map.of(
	                    "role", "user",
	                    "parts",
	                    List.of(
	                            Map.of(
	                                    "text",
	                                    userMessage
	                            )
	                    )
	            )
	    );

	    Map<String, Object> body = new HashMap<>();
	    body.put("contents", contents);

	    HttpHeaders header = new HttpHeaders();
	    header.setContentType(MediaType.APPLICATION_JSON);

	    HttpEntity<Map<String, Object>> entity =
	            new HttpEntity<>(body, header);

	    ResponseEntity<Map> response =
	            restTemplate.postForEntity(
	                    url,
	                    entity,
	                    Map.class
	            );

	    List candidates =
	            (List) response.getBody().get("candidates");

	    Map firstCandidate =
	            (Map) candidates.get(0);

	    Map content =
	            (Map) firstCandidate.get("content");

	    List parts =
	            (List) content.get("parts");

	    Map firstPart =
	            (Map) parts.get(0);

	    return firstPart.get("text").toString();
	}
}