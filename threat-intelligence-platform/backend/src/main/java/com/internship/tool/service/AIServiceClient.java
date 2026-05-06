package com.internship.tool.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;

@Service
public class AIServiceClient {

    private final RestTemplate restTemplate;

    // Constructor with 10s timeout
    public AIServiceClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // 10 seconds timeout (10000 ms)
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(10000);

        this.restTemplate = new RestTemplate(factory);
    }

    
    // TEST ENDPOINT (GET)
    
    public String callAIService() {
        try {
            String url = "http://localhost:5000/test";

            String response = restTemplate.getForObject(url, String.class);

            return response;

        } catch (Exception e) {
            return null; // as per requirement
        }
    }

    
    // RECOMMEND ENDPOINT (POST)
    
    public String getRecommendations(String userInput) {
        try {
            String url = "http://localhost:5000/recommend";

            // Request body
            Map<String, String> request = new HashMap<>();
            request.put("prompt", userInput);

            // POST call
            String response = restTemplate.postForObject(url, request, String.class);

            return response;

        } catch (Exception e) {
            return null; // as per requirement
        }
    }

    // OPTIONAL TEST (RUN ON START)
    @PostConstruct
    public void testCall() {
        String result = callAIService();
        System.out.println("AI RESPONSE FROM FLASK:");
        System.out.println(result);

        String recommend = getRecommendations("Suggest security improvements");
        System.out.println("RECOMMEND RESPONSE:");
        System.out.println(recommend); // will be null until endpoint exists
    }
}