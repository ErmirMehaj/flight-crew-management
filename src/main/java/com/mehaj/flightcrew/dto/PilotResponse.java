package com.mehaj.flightcrew.dto;

import com.mehaj.flightcrew.entity.PilotRank;
import com.mehaj.flightcrew.entity.PilotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PilotResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String licenseNumber;
    private String email;
    private String phoneNumber;
    private PilotRank rank;
    private PilotStatus status;
    private Double totalFlightHours;
}
