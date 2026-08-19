package com.mehaj.flightcrew.repository;

import com.mehaj.flightcrew.entity.Aircraft;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AircraftRepository extends JpaRepository<Aircraft, Long> {

    boolean existsByTailNumber(String tailNumber);
}
