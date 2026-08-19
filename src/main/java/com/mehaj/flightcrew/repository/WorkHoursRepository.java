package com.mehaj.flightcrew.repository;

import com.mehaj.flightcrew.entity.WorkHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface WorkHoursRepository extends JpaRepository<WorkHours, Long> {

    List<WorkHours> findByCrewMemberId(Long crewMemberId);

    /**
     * Total hours a crew member has logged since (and including) fromDate.
     * Used to enforce "crew members cannot exceed maximum working hours"
     * before creating a new FlightCrewAssignment. Returns 0 (not null)
     * when the crew member has no records, via COALESCE.
     */
    @Query("""
            SELECT COALESCE(SUM(w.hoursWorked), 0)
            FROM WorkHours w
            WHERE w.crewMember.id = :crewMemberId
              AND w.workDate >= :fromDate
            """)
    Double sumHoursWorkedSince(@Param("crewMemberId") Long crewMemberId,
                                @Param("fromDate") LocalDate fromDate);
}
