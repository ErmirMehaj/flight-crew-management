package com.mehaj.flightcrew.repository;

import com.mehaj.flightcrew.entity.FlightPilotAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlightPilotAssignmentRepository extends JpaRepository<FlightPilotAssignment, Long> {

    boolean existsByFlightIdAndPilotId(Long flightId, Long pilotId);

    List<FlightPilotAssignment> findByPilotId(Long pilotId);

    List<FlightPilotAssignment> findByFlightId(Long flightId);
}
