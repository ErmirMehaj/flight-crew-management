package com.mehaj.flightcrew.repository;

import com.mehaj.flightcrew.entity.Pilot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PilotRepository extends JpaRepository<Pilot, Long> {

    boolean existsByLicenseNumber(String licenseNumber);

    boolean existsByEmail(String email);
}
