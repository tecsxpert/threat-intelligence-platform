package com.internship.tool.controller;

import com.internship.tool.service.AIServiceClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AIController {

    private final AIServiceClient aiServiceClient;

    public AIController(AIServiceClient aiServiceClient) {
        this.aiServiceClient = aiServiceClient;
    }

    @GetMapping("/ai/test")
    public String testAI() {
        return aiServiceClient.callAIService();
    }
}