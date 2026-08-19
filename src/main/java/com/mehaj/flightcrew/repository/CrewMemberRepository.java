package com.mehaj.flightcrew.repository;

import com.mehaj.flightcrew.entity.CrewMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CrewMemberRepository extends JpaRepository<CrewMember, Long> {

    boolean existsByEmployeeId(String employeeId);

    boolean existsByEmail(String email);

    /**
     * ACTIVE crew members with no overlapping flight assignment during
     * [start, end), and whose already-logged hours since windowStart
     * plus this flight's duration would not exceed maxWeeklyHours.
     * windowStart and flightHours are computed in the service (simple
     * date/duration math, awkward and dialect-dependent in JPQL) and
     * passed in as bind parameters; the filtering/aggregation itself
     * stays in SQL via a correlated scalar subquery.
     */
    @Query("""
            SELECT c FROM CrewMember c
            WHERE c.status = com.mehaj.flightcrew.entity.CrewStatus.ACTIVE
              AND NOT EXISTS (
                  SELECT fca FROM FlightCrewAssignment fca
                  WHERE fca.crewMember = c
                    AND fca.flight.status <> com.mehaj.flightcrew.entity.FlightStatus.CANCELLED
                    AND fca.flight.departureTime < :end
                    AND fca.flight.arrivalTime > :start
              )
              AND (
                  COALESCE(
                      (SELECT SUM(w.hoursWorked) FROM WorkHours w
                       WHERE w.crewMember = c AND w.workDate >= :windowStart),
                      0
                  ) + :flightHours
              ) <= :maxWeeklyHours
            """)
    List<CrewMember> findAvailableCrew(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end,
                                        @Param("windowStart") LocalDate windowStart,
                                        @Param("flightHours") double flightHours,
                                        @Param("maxWeeklyHours") double maxWeeklyHours);
}
