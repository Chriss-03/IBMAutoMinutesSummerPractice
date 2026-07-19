package com.autominutes.dto;

import jakarta.validation.constraints.NotBlank;

public record PromptTemplateRequest(
        @NotBlank(message = "Prompt template name must not be blank")
        String name,

        @NotBlank(message = "Prompt text must not be blank")
        String promptText,

        Boolean active
) {
}
