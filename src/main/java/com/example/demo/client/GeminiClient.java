package com.example.demo.client;

import com.example.demo.config.GeminiProperties;
import com.example.demo.dto.AiAnalysisResult;
import com.example.demo.dto.gemini.GeminiRequest;
import com.example.demo.dto.gemini.GeminiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Client for interacting with Google Gemini API
 * Handles API calls, retries, and error handling
 */
@Component
@Slf4j
public class GeminiClient {

    private final GeminiProperties geminiProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiClient(GeminiProperties geminiProperties, ObjectMapper objectMapper) {
        this.geminiProperties = geminiProperties;
        this.objectMapper = objectMapper;
        
        // Create RestTemplate with timeout
        this.restTemplate = new RestTemplate();
        this.restTemplate.setRequestFactory(
            new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
                setConnectTimeout(Duration.ofSeconds(geminiProperties.getTimeoutSeconds()));
                setReadTimeout(Duration.ofSeconds(geminiProperties.getTimeoutSeconds()));
            }}
        );
    }

    /**
     * Generate content using Gemini API
     * 
     * @param prompt The prompt to send to Gemini
     * @return GeminiResponse containing the generated content
     */
    public GeminiResponse generateContent(String prompt) {
        if (!geminiProperties.isConfigured()) {
            log.error("Gemini API is not properly configured");
            return null;
        }

        try {
            String url = geminiProperties.getGenerateContentUrl();
            
            GeminiRequest request = GeminiRequest.create(
                prompt,
                geminiProperties.getTemperature(),
                geminiProperties.getMaxTokens()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

            log.debug("Calling Gemini API: {}", url);
            log.debug("Request: {}", objectMapper.writeValueAsString(request));

            ResponseEntity<GeminiResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                GeminiResponse.class
            );

            GeminiResponse responseBody = response.getBody();
            
            if (responseBody != null && responseBody.isSuccessful()) {
                log.debug("Gemini API response: {}", responseBody.getTextResponse());
                return responseBody;
            } else {
                log.warn("Gemini API returned empty or unsuccessful response");
                return null;
            }

        } catch (RestClientException e) {
            log.error("Error calling Gemini API", e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error with Gemini API", e);
            return null;
        }
    }

    /**
     * Generate content with system prompt
     * 
     * @param systemPrompt System instructions for the AI
     * @param userMessage User's message
     * @return GeminiResponse containing the generated content
     */
    public GeminiResponse generateContentWithSystemPrompt(String systemPrompt, String userMessage) {
        if (!geminiProperties.isConfigured()) {
            log.error("Gemini API is not properly configured");
            return null;
        }

        try {
            String url = geminiProperties.getGenerateContentUrl();
            
            GeminiRequest request = GeminiRequest.createWithSystemPrompt(
                systemPrompt,
                userMessage,
                geminiProperties.getTemperature(),
                geminiProperties.getMaxTokens()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

            log.debug("Calling Gemini API with system prompt");

            ResponseEntity<GeminiResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                GeminiResponse.class
            );

            GeminiResponse responseBody = response.getBody();
            
            if (responseBody != null && responseBody.isSuccessful()) {
                log.debug("Gemini API response received successfully");
                return responseBody;
            } else {
                log.warn("Gemini API returned empty or unsuccessful response");
                return null;
            }

        } catch (RestClientException e) {
            log.error("Error calling Gemini API", e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error with Gemini API", e);
            return null;
        }
    }

    /**
     * Analyze message and get structured AI response
     * 
     * @param systemPrompt System instructions
     * @param userMessage User's message
     * @return AiAnalysisResult parsed from JSON response
     */
    public AiAnalysisResult analyzeMessage(String systemPrompt, String userMessage) {
        GeminiResponse response = generateContentWithSystemPrompt(systemPrompt, userMessage);
        
        if (response == null || !response.isSuccessful()) {
            log.error("Failed to get response from Gemini");
            return null;
        }

        try {
            String jsonResponse = response.getTextResponse();
            log.debug("Parsing AI analysis from JSON: {}", jsonResponse);
            
            // Parse JSON response to AiAnalysisResult
            AiAnalysisResult result = objectMapper.readValue(jsonResponse, AiAnalysisResult.class);
            
            log.info("AI Analysis - Intent: {}, Confidence: {}, Handoff: {}", 
                result.getIntent(), result.getConfidence(), result.getNeedHandoff());
            
            return result;
            
        } catch (Exception e) {
            log.error("Error parsing AI analysis result", e);
            return null;
        }
    }

    /**
     * Generate content with retry logic
     * 
     * @param prompt The prompt
     * @param maxRetries Maximum number of retries
     * @return GeminiResponse or null if all retries fail
     */
    public GeminiResponse generateContentWithRetry(String prompt, int maxRetries) {
        int attempts = 0;
        GeminiResponse response = null;

        while (attempts < maxRetries && response == null) {
            attempts++;
            log.debug("Gemini API call attempt {} of {}", attempts, maxRetries);
            
            response = generateContent(prompt);
            
            if (response == null && attempts < maxRetries) {
                try {
                    // Wait before retry (exponential backoff)
                    Thread.sleep(1000L * attempts);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        if (response == null) {
            log.error("Failed to get response from Gemini after {} attempts", maxRetries);
        }

        return response;
    }

    /**
     * Check if Gemini API is available
     */
    public boolean isAvailable() {
        return geminiProperties.isConfigured();
    }
}

