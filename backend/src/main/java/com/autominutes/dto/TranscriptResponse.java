package com.autominutes.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record TranscriptResponse(
        UUID id,
        UUID meetingId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
