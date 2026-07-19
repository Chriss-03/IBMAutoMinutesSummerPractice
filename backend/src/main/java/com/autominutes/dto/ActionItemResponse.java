package com.autominutes.dto;

import com.autominutes.enums.ActionItemStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ActionItemResponse(
        UUID id,
        String description,
        String proposedAssignee,
        LocalDate deadline,
        ActionItemStatus status,
        UUID meetingId,
        UUID aiResultId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
