package com.internship.tool.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;

import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;

@Service
public class AIServiceClient {

    private final RestTemplate restTemplate;

    public AIServiceClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);
        this.restTemplate = new RestTemplate(factory);
    }

    public Map<String, Object> callAIService() {
        return callAIServiceWithPrompt("what is ai?");
    }

    public Map<String, Object> callAIServiceWithPrompt(String prompt) {
        try {
            String url = "http://localhost:5000/test";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer secure-token-123");

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("prompt", prompt);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            return response.getBody() != null ? response.getBody() : buildError("Empty response from AI service");

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            try {
                String responseBody = e.getResponseBodyAsString();

                Map<String, Object> error = new HashMap<>();
                error.put("status", "blocked");
                error.put("message", responseBody);

                return error;

            } catch (Exception ex) {
                return buildError("Blocked request (sanitizer triggered)");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return buildError("Failed to call AI service");
        }
    }

    public Map<String, Object> getRecommendations(String userInput) {
        try {
            String url = "http://localhost:5000/recommend";

            Map<String, String> request = new HashMap<>();
            request.put("prompt", userInput);

            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                    url,
                    request,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            return response.getBody() != null ? response.getBody() : buildError("Empty recommendation response");

        } catch (Exception e) {
            return buildError("Recommendation failed");
        }
    }

    private Map<String, Object> buildError(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", message);
        return error;
    }

    @PostConstruct
    public void testCall() {

        Map<String, Object> result = callAIService();
        System.out.println("AI RESPONSE:");
        System.out.println(result);

        Map<String, Object> recommend = getRecommendations("Suggest security improvements");
        System.out.println("RECOMMEND RESPONSE:");
        System.out.println(recommend);
    }
}