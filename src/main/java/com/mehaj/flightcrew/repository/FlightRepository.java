package com.mehaj.flightcrew.repository;

import com.mehaj.flightcrew.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    boolean existsByFlightNumber(String flightNumber);

    /**
     * Flights already using this aircraft whose time window overlaps the
     * given [departureTime, arrivalTime) window. Cancelled flights are
     * excluded since a cancelled flight no longer occupies the aircraft.
     * Used to enforce "aircraft cannot be assigned to overlapping flights."
     */
    @Query("""
            SELECT f FROM Flight f
            WHERE f.aircraft.id = :aircraftId
              AND f.status <> com.mehaj.flightcrew.entity.FlightStatus.CANCELLED
              AND f.departureTime < :arrivalTime
              AND f.arrivalTime > :departureTime
            """)
    List<Flight> findOverlappingFlightsForAircraft(@Param("aircraftId") Long aircraftId,
                                                     @Param("departureTime") LocalDateTime departureTime,
                                                     @Param("arrivalTime") LocalDateTime arrivalTime);
}
