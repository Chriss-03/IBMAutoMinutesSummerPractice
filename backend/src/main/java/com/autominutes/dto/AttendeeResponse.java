package com.autominutes.dto;

import java.util.UUID;

public record AttendeeResponse(
        UUID id,
        String name,
        String email,
        String role,
        UUID meetingId
) {
}
