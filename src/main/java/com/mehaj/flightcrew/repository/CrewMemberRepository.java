package com.mehaj.flightcrew.repository;

import com.mehaj.flightcrew.entity.CrewMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrewMemberRepository extends JpaRepository<CrewMember, Long> {

    boolean existsByEmployeeId(String employeeId);

    boolean existsByEmail(String email);
}
