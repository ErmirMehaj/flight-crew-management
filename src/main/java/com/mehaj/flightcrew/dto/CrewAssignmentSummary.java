package com.mehaj.flightcrew.dto;

import com.mehaj.flightcrew.entity.CrewPosition;
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
public class CrewAssignmentSummary {

    private Long crewMemberId;
    private String firstName;
    private String lastName;
    private CrewPosition role;
}
