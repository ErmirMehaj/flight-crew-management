package com.mehaj.flightcrew.mapper;

import com.mehaj.flightcrew.dto.CrewAssignmentSummary;
import com.mehaj.flightcrew.dto.FlightCreateRequest;
import com.mehaj.flightcrew.dto.FlightResponse;
import com.mehaj.flightcrew.dto.FlightUpdateRequest;
import com.mehaj.flightcrew.dto.PilotAssignmentSummary;
import com.mehaj.flightcrew.entity.Flight;
import com.mehaj.flightcrew.entity.FlightStatus;
import org.springframework.stereotype.Component;

@Component
public class FlightMapper {

    public Flight toEntity(FlightCreateRequest request) {
        Flight flight = new Flight();
        flight.setFlightNumber(request.getFlightNumber());
        flight.setDepartureAirport(request.getDepartureAirport());
        flight.setArrivalAirport(request.getArrivalAirport());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        flight.setStatus(FlightStatus.SCHEDULED);
        return flight;
    }

    public void updateEntity(Flight flight, FlightUpdateRequest request) {
        flight.setFlightNumber(request.getFlightNumber());
        flight.setDepartureAirport(request.getDepartureAirport());
        flight.setArrivalAirport(request.getArrivalAirport());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
    }

    public FlightResponse toResponse(Flight flight) {
        return FlightResponse.builder()
                .id(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .departureAirport(flight.getDepartureAirport())
                .arrivalAirport(flight.getArrivalAirport())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .status(flight.getStatus())
                .aircraftId(flight.getAircraft() != null ? flight.getAircraft().getId() : null)
                .aircraftTailNumber(flight.getAircraft() != null ? flight.getAircraft().getTailNumber() : null)
                .pilots(flight.getPilotAssignments().stream()
                        .map(a -> PilotAssignmentSummary.builder()
                                .pilotId(a.getPilot().getId())
                                .firstName(a.getPilot().getFirstName())
                                .lastName(a.getPilot().getLastName())
                                .role(a.getRole())
                                .build())
                        .toList())
                .crew(flight.getCrewAssignments().stream()
                        .map(a -> CrewAssignmentSummary.builder()
                                .crewMemberId(a.getCrewMember().getId())
                                .firstName(a.getCrewMember().getFirstName())
                                .lastName(a.getCrewMember().getLastName())
                                .role(a.getRole())
                                .build())
                        .toList())
                .build();
    }
}
