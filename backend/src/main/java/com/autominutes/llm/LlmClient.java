package com.autominutes.llm;

public interface LlmClient {
    LlmMeetingResult processTranscript(String prompt, String transcript);
}
