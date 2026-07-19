package com.autominutes.dto;

import com.autominutes.enums.ActionItemStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateActionItemRequest(
        @NotBlank(message = "Action item description must not be blank")
        String description,

        String proposedAssignee,

        LocalDate deadline,

        @NotNull(message = "Action item status is required")
        ActionItemStatus status
) {
}
