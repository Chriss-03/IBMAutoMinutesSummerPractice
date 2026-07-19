package com.autominutes.llm;

public record LlmActionItem(
        String description,
        String proposedAssignee,
        String deadline,
        String status
) {
}
