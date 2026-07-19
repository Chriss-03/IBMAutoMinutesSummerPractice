package com.autominutes.dto;

import jakarta.validation.constraints.NotBlank;

public record TranscriptRequest(
        @NotBlank(message = "Transcript content must not be blank")
        String content
) {
}
