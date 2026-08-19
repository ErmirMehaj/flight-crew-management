package com.mehaj.flightcrew.service;

import com.mehaj.flightcrew.dto.CrewMemberCreateRequest;
import com.mehaj.flightcrew.dto.CrewMemberResponse;
import com.mehaj.flightcrew.dto.CrewMemberUpdateRequest;
import com.mehaj.flightcrew.entity.CrewMember;
import com.mehaj.flightcrew.exception.DuplicateResourceException;
import com.mehaj.flightcrew.exception.ResourceNotFoundException;
import com.mehaj.flightcrew.mapper.CrewMemberMapper;
import com.mehaj.flightcrew.repository.CrewMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CrewMemberService {

    private final CrewMemberRepository crewMemberRepository;
    private final CrewMemberMapper crewMemberMapper;

    @Transactional
    public CrewMemberResponse createCrewMember(CrewMemberCreateRequest request) {
        if (crewMemberRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new DuplicateResourceException(
                    "Crew member with employee ID '" + request.getEmployeeId() + "' already exists");
        }
        if (crewMemberRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Crew member with email '" + request.getEmail() + "' already exists");
        }

        CrewMember crewMember = crewMemberMapper.toEntity(request);
        CrewMember saved = crewMemberRepository.save(crewMember);
        log.info("Created crew member id={} employeeId={}", saved.getId(), saved.getEmployeeId());
        return crewMemberMapper.toResponse(saved);
    }

    public CrewMemberResponse getCrewMemberById(Long id) {
        return crewMemberMapper.toResponse(findCrewMemberOrThrow(id));
    }

    public List<CrewMemberResponse> getAllCrewMembers() {
        return crewMemberRepository.findAll().stream()
                .map(crewMemberMapper::toResponse)
                .toList();
    }

    @Transactional
    public CrewMemberResponse updateCrewMember(Long id, CrewMemberUpdateRequest request) {
        CrewMember crewMember = findCrewMemberOrThrow(id);

        boolean employeeIdChanged = !crewMember.getEmployeeId().equals(request.getEmployeeId());
        if (employeeIdChanged && crewMemberRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new DuplicateResourceException(
                    "Crew member with employee ID '" + request.getEmployeeId() + "' already exists");
        }

        boolean emailChanged = !crewMember.getEmail().equals(request.getEmail());
        if (emailChanged && crewMemberRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Crew member with email '" + request.getEmail() + "' already exists");
        }

        crewMemberMapper.updateEntity(crewMember, request);
        CrewMember saved = crewMemberRepository.save(crewMember);
        log.info("Updated crew member id={}", saved.getId());
        return crewMemberMapper.toResponse(saved);
    }

    @Transactional
    public void deleteCrewMember(Long id) {
        CrewMember crewMember = findCrewMemberOrThrow(id);
        crewMemberRepository.delete(crewMember);
        log.info("Deleted crew member id={}", id);
    }

    private CrewMember findCrewMemberOrThrow(Long id) {
        return crewMemberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crew member not found with id " + id));
    }
}
