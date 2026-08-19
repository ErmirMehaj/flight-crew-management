package com.mehaj.flightcrew.dto;

import com.mehaj.flightcrew.entity.CrewPosition;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrewMemberCreateRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    private String phoneNumber;

    @NotNull(message = "Position is required")
    private CrewPosition position;
}
