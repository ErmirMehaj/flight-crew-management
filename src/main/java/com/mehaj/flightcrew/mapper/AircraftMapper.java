package com.mehaj.flightcrew.mapper;

import com.mehaj.flightcrew.dto.AircraftCreateRequest;
import com.mehaj.flightcrew.dto.AircraftResponse;
import com.mehaj.flightcrew.dto.AircraftUpdateRequest;
import com.mehaj.flightcrew.entity.Aircraft;
import com.mehaj.flightcrew.entity.AircraftStatus;
import org.springframework.stereotype.Component;

@Component
public class AircraftMapper {

    public Aircraft toEntity(AircraftCreateRequest request) {
        Aircraft aircraft = new Aircraft();
        aircraft.setTailNumber(request.getTailNumber());
        aircraft.setModel(request.getModel());
        aircraft.setCapacity(request.getCapacity());
        aircraft.setStatus(AircraftStatus.ACTIVE);
        return aircraft;
    }

    public void updateEntity(Aircraft aircraft, AircraftUpdateRequest request) {
        aircraft.setTailNumber(request.getTailNumber());
        aircraft.setModel(request.getModel());
        aircraft.setCapacity(request.getCapacity());
        aircraft.setStatus(request.getStatus());
    }

    public AircraftResponse toResponse(Aircraft aircraft) {
        return AircraftResponse.builder()
                .id(aircraft.getId())
                .tailNumber(aircraft.getTailNumber())
                .model(aircraft.getModel())
                .capacity(aircraft.getCapacity())
                .status(aircraft.getStatus())
                .build();
    }
}
