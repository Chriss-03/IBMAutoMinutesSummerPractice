package com.autominutes.llm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "llm.enabled", havingValue = "false", matchIfMissing = true)
public class MockLlmClient implements LlmClient {
    @Override
    public LlmMeetingResult processTranscript(String prompt, String transcript) {
        String raw = "{\"provider\":\"mock\",\"reason\":\"llm.enabled is false\"}";
        return new LlmMeetingResult(
                "Mock summary: the transcript was received and processed locally.",
                List.of(
                        "The meeting transcript was stored and passed to the AI processing workflow.",
                        "This mock result proves that AIResult and ActionItem persistence works without a real API key."
                ),
                List.of("Transcript processing workflow", "Backend persistence", "Action item extraction"),
                List.of("Use a real LLM provider when an API key is configured."),
                List.of("Replace the mock client by setting LLM_ENABLED=true."),
                List.of(new LlmActionItem(
                        "Configure a real LLM API key for production processing.",
                        "Backend team",
                        null,
                        "OPEN"
                )),
                raw
        );
    }
}
