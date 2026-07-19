package com.autominutes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateAttendeeRequest(
        @NotBlank(message = "Attendee name must not be blank")
        String name,

        @Email(message = "Attendee email must be valid")
        String email,

        String role
) {
}
