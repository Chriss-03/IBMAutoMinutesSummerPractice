package com.autominutes.llm;

import com.autominutes.exception.AiProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "llm.enabled", havingValue = "true")
public class OpenAiCompatibleLlmClient implements LlmClient {
    private final LlmProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public OpenAiCompatibleLlmClient(LlmProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public LlmMeetingResult processTranscript(String prompt, String transcript) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new AiProcessingException("LLM API key is missing. Set LLM_API_KEY or disable real LLM mode.");
        }

        Map<String, Object> body = Map.of(
            "model", properties.getModel(),
            "temperature", properties.getTemperature(),
            "stream", false,
            "response_format", Map.of("type", "json_object"),
            "messages", List.of(
                    Map.of("role", "system", "content", prompt),
                    Map.of("role", "user", "content", transcript)
            )
    );

        try {
            String response = restClient.post()
                    .uri("/chat/completions")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            if (content == null || content.isBlank()) {
                throw new AiProcessingException("LLM response did not contain message content");
            }

            LlmMeetingResult parsed = objectMapper.readValue(content, LlmMeetingResult.class);
            return new LlmMeetingResult(
                    parsed.summary(),
                    parsed.detailedSummary(),
                    parsed.keyDiscussionPoints(),
                    parsed.decisions(),
                    parsed.followUpNotes(),
                    parsed.actionItems(),
                    response
            );
        } catch (AiProcessingException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AiProcessingException("Failed to call or parse LLM response", exception);
        }
    }
}
