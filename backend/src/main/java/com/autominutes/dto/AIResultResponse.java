package com.autominutes.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AIResultResponse(
        UUID id,
        UUID meetingId,
        UUID promptTemplateId,
        String summary,
        String detailedSummary,
        String keyDiscussionPoints,
        String decisions,
        String followUpNotes,
        Integer versionNumber,
        Boolean latest,
        LocalDateTime createdAt,
        List<ActionItemResponse> actionItems
) {
}
