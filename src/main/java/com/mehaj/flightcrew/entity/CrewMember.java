package com.mehaj.flightcrew.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "crew_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = "workHours")
public class CrewMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String employeeId;

    @Column(nullable = false, unique = true)
    private String email;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CrewPosition position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CrewStatus status;

    @OneToMany(mappedBy = "crewMember", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkHours> workHours = new ArrayList<>();
}
