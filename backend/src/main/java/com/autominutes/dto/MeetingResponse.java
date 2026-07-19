package com.autominutes.dto;

import com.autominutes.enums.ProcessingStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record MeetingResponse(
        UUID id,
        String title,
        LocalDateTime meetingDateTime,
        String description,
        ProcessingStatus processingStatus,
        boolean transcriptPresent,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
