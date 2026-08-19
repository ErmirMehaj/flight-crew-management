package com.mehaj.flightcrew.repository;

import com.mehaj.flightcrew.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findByPilotId(Long pilotId);

    /**
     * True if the pilot has an unavailability block overlapping
     * [start, end). Used to enforce "a pilot cannot be assigned if
     * unavailable" before creating a FlightPilotAssignment.
     */
    @Query("""
            SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
            FROM Availability a
            WHERE a.pilot.id = :pilotId
              AND a.startTime < :end
              AND a.endTime > :start
            """)
    boolean existsOverlappingUnavailability(@Param("pilotId") Long pilotId,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);
}
