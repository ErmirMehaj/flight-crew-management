package com.mehaj.flightcrew.mapper;

import com.mehaj.flightcrew.dto.CrewMemberCreateRequest;
import com.mehaj.flightcrew.dto.CrewMemberResponse;
import com.mehaj.flightcrew.dto.CrewMemberUpdateRequest;
import com.mehaj.flightcrew.entity.CrewMember;
import com.mehaj.flightcrew.entity.CrewStatus;
import org.springframework.stereotype.Component;

@Component
public class CrewMemberMapper {

    public CrewMember toEntity(CrewMemberCreateRequest request) {
        CrewMember crewMember = new CrewMember();
        crewMember.setFirstName(request.getFirstName());
        crewMember.setLastName(request.getLastName());
        crewMember.setEmployeeId(request.getEmployeeId());
        crewMember.setEmail(request.getEmail());
        crewMember.setPhoneNumber(request.getPhoneNumber());
        crewMember.setPosition(request.getPosition());
        crewMember.setStatus(CrewStatus.ACTIVE);
        return crewMember;
    }

    public void updateEntity(CrewMember crewMember, CrewMemberUpdateRequest request) {
        crewMember.setFirstName(request.getFirstName());
        crewMember.setLastName(request.getLastName());
        crewMember.setEmployeeId(request.getEmployeeId());
        crewMember.setEmail(request.getEmail());
        crewMember.setPhoneNumber(request.getPhoneNumber());
        crewMember.setPosition(request.getPosition());
        crewMember.setStatus(request.getStatus());
    }

    public CrewMemberResponse toResponse(CrewMember crewMember) {
        return CrewMemberResponse.builder()
                .id(crewMember.getId())
                .firstName(crewMember.getFirstName())
                .lastName(crewMember.getLastName())
                .employeeId(crewMember.getEmployeeId())
                .email(crewMember.getEmail())
                .phoneNumber(crewMember.getPhoneNumber())
                .position(crewMember.getPosition())
                .status(crewMember.getStatus())
                .build();
    }
}
