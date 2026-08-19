package com.mehaj.flightcrew.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "flight_pilot_assignments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"flight_id", "pilot_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"flight", "pilot"})
public class FlightPilotAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pilot_id", nullable = false)
    private Pilot pilot;

    // The role this pilot is fulfilling on this specific flight.
    // Usually matches Pilot.rank, but kept separate so a captain-ranked
    // pilot could serve as first officer on a given flight if needed.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PilotRank role;

    @Column(nullable = false)
    private LocalDateTime assignedAt;
}
