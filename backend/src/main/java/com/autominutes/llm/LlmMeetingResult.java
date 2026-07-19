package com.autominutes.llm;

import java.util.List;

public record LlmMeetingResult(
        String summary,
        List<String> detailedSummary,
        List<String> keyDiscussionPoints,
        List<String> decisions,
        List<String> followUpNotes,
        List<LlmActionItem> actionItems,
        String rawResponse
) {
}
