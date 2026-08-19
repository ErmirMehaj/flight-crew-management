package com.mehaj.flightcrew.repository;

import com.mehaj.flightcrew.entity.FlightPilotAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FlightPilotAssignmentRepository extends JpaRepository<FlightPilotAssignment, Long> {

    boolean existsByFlightIdAndPilotId(Long flightId, Long pilotId);

    List<FlightPilotAssignment> findByPilotId(Long pilotId);

    List<FlightPilotAssignment> findByFlightId(Long flightId);

    /**
     * True if this pilot is already assigned to a (non-cancelled) flight
     * whose time window overlaps [start, end) -- the same double-booking
     * check used for aircraft, applied to a person instead of a plane.
     */
    @Query("""
            SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
            FROM FlightPilotAssignment a
            WHERE a.pilot.id = :pilotId
              AND a.flight.status <> com.mehaj.flightcrew.entity.FlightStatus.CANCELLED
              AND a.flight.departureTime < :end
              AND a.flight.arrivalTime > :start
            """)
    boolean existsOverlappingAssignment(@Param("pilotId") Long pilotId,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);
}
