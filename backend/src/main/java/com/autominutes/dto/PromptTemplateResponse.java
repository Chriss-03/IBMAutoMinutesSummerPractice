package com.autominutes.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PromptTemplateResponse(
        UUID id,
        String name,
        String promptText,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
