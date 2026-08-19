package com.mehaj.flightcrew.dto;

import com.mehaj.flightcrew.entity.CrewPosition;
import com.mehaj.flightcrew.entity.CrewStatus;
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
public class CrewMemberResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String employeeId;
    private String email;
    private String phoneNumber;
    private CrewPosition position;
    private CrewStatus status;
}
