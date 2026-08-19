package com.mehaj.flightcrew.repository;

import com.mehaj.flightcrew.entity.Aircraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AircraftRepository extends JpaRepository<Aircraft, Long> {

    boolean existsByTailNumber(String tailNumber);

    /**
     * ACTIVE aircraft with no non-cancelled flight overlapping
     * [start, end). One query via a correlated NOT EXISTS subquery,
     * rather than fetching every active aircraft and checking each one
     * individually -- the candidate set here is the whole fleet, not a
     * handful of roster entries, so pushing the filter into SQL avoids
     * a real N+1.
     */
    @Query("""
            SELECT a FROM Aircraft a
            WHERE a.status = com.mehaj.flightcrew.entity.AircraftStatus.ACTIVE
              AND NOT EXISTS (
                  SELECT f FROM Flight f
                  WHERE f.aircraft = a
                    AND f.status <> com.mehaj.flightcrew.entity.FlightStatus.CANCELLED
                    AND f.departureTime < :end
                    AND f.arrivalTime > :start
              )
            """)
    List<Aircraft> findAvailableAircraft(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);
}
