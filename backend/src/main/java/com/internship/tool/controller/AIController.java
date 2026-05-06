package com.internship.tool.controller;

import com.internship.tool.service.AIServiceClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;

import java.util.Map;

@RestController
public class AIController {

    private final AIServiceClient aiServiceClient;

    public AIController(AIServiceClient aiServiceClient) {
        this.aiServiceClient = aiServiceClient;
    }

    @GetMapping("/ai/test")
    public ResponseEntity<Map<String, Object>> testAI() {
        Map<String, Object> response = aiServiceClient.callAIService();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/")
    public String home() {
        return "Backend is running";
    }
}