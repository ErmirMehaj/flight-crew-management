package com.mehaj.flightcrew.mapper;

import com.mehaj.flightcrew.dto.PilotCreateRequest;
import com.mehaj.flightcrew.dto.PilotResponse;
import com.mehaj.flightcrew.dto.PilotUpdateRequest;
import com.mehaj.flightcrew.entity.Pilot;
import com.mehaj.flightcrew.entity.PilotStatus;
import org.springframework.stereotype.Component;

@Component
public class PilotMapper {

    public Pilot toEntity(PilotCreateRequest request) {
        Pilot pilot = new Pilot();
        pilot.setFirstName(request.getFirstName());
        pilot.setLastName(request.getLastName());
        pilot.setLicenseNumber(request.getLicenseNumber());
        pilot.setEmail(request.getEmail());
        pilot.setPhoneNumber(request.getPhoneNumber());
        pilot.setRank(request.getRank());
        // New pilots always start ACTIVE; status is only changed via update.
        pilot.setStatus(PilotStatus.ACTIVE);
        pilot.setTotalFlightHours(0.0);
        return pilot;
    }

    public void updateEntity(Pilot pilot, PilotUpdateRequest request) {
        pilot.setFirstName(request.getFirstName());
        pilot.setLastName(request.getLastName());
        pilot.setLicenseNumber(request.getLicenseNumber());
        pilot.setEmail(request.getEmail());
        pilot.setPhoneNumber(request.getPhoneNumber());
        pilot.setRank(request.getRank());
        pilot.setStatus(request.getStatus());
    }

    public PilotResponse toResponse(Pilot pilot) {
        return PilotResponse.builder()
                .id(pilot.getId())
                .firstName(pilot.getFirstName())
                .lastName(pilot.getLastName())
                .licenseNumber(pilot.getLicenseNumber())
                .email(pilot.getEmail())
                .phoneNumber(pilot.getPhoneNumber())
                .rank(pilot.getRank())
                .status(pilot.getStatus())
                .totalFlightHours(pilot.getTotalFlightHours())
                .build();
    }
}
