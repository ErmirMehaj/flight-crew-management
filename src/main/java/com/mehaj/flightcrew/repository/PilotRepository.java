package com.mehaj.flightcrew.repository;

import com.mehaj.flightcrew.entity.Pilot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PilotRepository extends JpaRepository<Pilot, Long> {

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByEmail(String email);

    /**
     * ACTIVE pilots with no overlapping unavailability block and no
     * overlapping flight assignment during [start, end) -- the same two
     * checks assignPilot enforces on a single candidate, applied here as
     * a listing filter across every pilot.
     */
    @Query("""
            SELECT p FROM Pilot p
            WHERE p.status = com.mehaj.flightcrew.entity.PilotStatus.ACTIVE
              AND NOT EXISTS (
                  SELECT av FROM Availability av
                  WHERE av.pilot = p
                    AND av.startTime < :end
                    AND av.endTime > :start
              )
              AND NOT EXISTS (
                  SELECT fpa FROM FlightPilotAssignment fpa
                  WHERE fpa.pilot = p
                    AND fpa.flight.status <> com.mehaj.flightcrew.entity.FlightStatus.CANCELLED
                    AND fpa.flight.departureTime < :end
                    AND fpa.flight.arrivalTime > :start
              )
            """)
    List<Pilot> findAvailablePilots(@Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);
}
