package com.mehaj.flightcrew.dto;

import com.mehaj.flightcrew.entity.AircraftStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AircraftUpdateRequest {

    @NotBlank(message = "Tail number is required")
    private String tailNumber;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be greater than zero")
    private Integer capacity;

    @NotNull(message = "Status is required")
    private AircraftStatus status;
}
