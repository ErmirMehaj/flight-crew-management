package com.mehaj.flightcrew.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * One logged record of hours worked by a crew member on a specific flight.
 * Created when a flight is marked COMPLETED. The sum of hoursWorked for a
 * crew member over a rolling window is checked against the configured max
 * weekly hours before a new assignment is allowed.
 */
@Entity
@Table(name = "work_hours",
        uniqueConstraints = @UniqueConstraint(columnNames = {"crew_member_id", "flight_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"crewMember", "flight"})
public class WorkHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_member_id", nullable = false)
    private CrewMember crewMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @Column(nullable = false)
    private Double hoursWorked;

    @Column(nullable = false)
    private LocalDate workDate;
}
