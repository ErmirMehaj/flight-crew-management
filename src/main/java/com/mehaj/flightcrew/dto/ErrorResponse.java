package com.mehaj.flightcrew.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    // Only populated for validation failures (400 from @Valid); null otherwise.
    private List<FieldError> fieldErrors;

    public record FieldError(String field, String message) {
    }
}
