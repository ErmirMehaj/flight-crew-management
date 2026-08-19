package com.mehaj.flightcrew.repository;

import com.mehaj.flightcrew.entity.FlightCrewAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlightCrewAssignmentRepository extends JpaRepository<FlightCrewAssignment, Long> {

    boolean existsByFlightIdAndCrewMemberId(Long flightId, Long crewMemberId);

    List<FlightCrewAssignment> findByCrewMemberId(Long crewMemberId);

    List<FlightCrewAssignment> findByFlightId(Long flightId);
}
