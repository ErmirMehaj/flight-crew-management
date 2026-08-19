package com.mehaj.flightcrew.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "flight_crew_assignments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"flight_id", "crew_member_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"flight", "crewMember"})
public class FlightCrewAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crew_member_id", nullable = false)
    private CrewMember crewMember;

    // The role this crew member is fulfilling on this specific flight.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CrewPosition role;

    @Column(nullable = false)
    private LocalDateTime assignedAt;
}
