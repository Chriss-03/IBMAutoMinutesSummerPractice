package com.autominutes.dto;

public record FieldErrorResponse(
        String field,
        String message
) {
}
