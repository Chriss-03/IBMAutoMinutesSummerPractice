package com.autominutes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateMeetingRequest(
        @NotBlank(message = "Meeting title must not be blank")
        String title,

        @NotNull(message = "Meeting date/time is required")
        LocalDateTime meetingDateTime,

        String description
) {
}
