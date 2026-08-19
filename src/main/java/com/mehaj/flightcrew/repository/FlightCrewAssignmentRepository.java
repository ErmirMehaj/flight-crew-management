package com.mehaj.flightcrew.repository;

import com.mehaj.flightcrew.entity.FlightCrewAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FlightCrewAssignmentRepository extends JpaRepository<FlightCrewAssignment, Long> {

    boolean existsByFlightIdAndCrewMemberId(Long flightId, Long crewMemberId);

    List<FlightCrewAssignment> findByCrewMemberId(Long crewMemberId);

    List<FlightCrewAssignment> findByFlightId(Long flightId);

    /**
     * True if this crew member is already assigned to a (non-cancelled)
     * flight whose time window overlaps [start, end).
     */
    @Query("""
            SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
            FROM FlightCrewAssignment a
            WHERE a.crewMember.id = :crewMemberId
              AND a.flight.status <> com.mehaj.flightcrew.entity.FlightStatus.CANCELLED
              AND a.flight.departureTime < :end
              AND a.flight.arrivalTime > :start
            """)
    boolean existsOverlappingAssignment(@Param("crewMemberId") Long crewMemberId,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);
}
