package com.mehaj.flightcrew.service;

import com.mehaj.flightcrew.dto.CrewMemberCreateRequest;
import com.mehaj.flightcrew.dto.CrewMemberResponse;
import com.mehaj.flightcrew.dto.CrewMemberUpdateRequest;
import com.mehaj.flightcrew.entity.CrewMember;
import com.mehaj.flightcrew.entity.CrewPosition;
import com.mehaj.flightcrew.entity.CrewStatus;
import com.mehaj.flightcrew.exception.DuplicateResourceException;
import com.mehaj.flightcrew.exception.ResourceNotFoundException;
import com.mehaj.flightcrew.mapper.CrewMemberMapper;
import com.mehaj.flightcrew.repository.CrewMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrewMemberServiceTest {

    @Mock
    private CrewMemberRepository crewMemberRepository;

    @Mock
    private CrewMemberMapper crewMemberMapper;

    @InjectMocks
    private CrewMemberService crewMemberService;

    private CrewMember existingCrewMember;

    @BeforeEach
    void setUp() {
        existingCrewMember = new CrewMember();
        existingCrewMember.setId(1L);
        existingCrewMember.setFirstName("Alex");
        existingCrewMember.setLastName("Chen");
        existingCrewMember.setEmployeeId("EMP-001");
        existingCrewMember.setEmail("alex.chen@example.com");
        existingCrewMember.setPosition(CrewPosition.PURSER);
        existingCrewMember.setStatus(CrewStatus.ACTIVE);
    }

    @Test
    void createCrewMember_savesAndReturnsResponse_whenEmployeeIdAndEmailAreUnique() {
        CrewMemberCreateRequest request = new CrewMemberCreateRequest();
        request.setEmployeeId("EMP-002");
        request.setEmail("new.crew@example.com");

        CrewMember mappedEntity = new CrewMember();
        CrewMember savedEntity = new CrewMember();
        savedEntity.setId(2L);
        CrewMemberResponse expectedResponse = CrewMemberResponse.builder().id(2L).build();

        when(crewMemberRepository.existsByEmployeeId("EMP-002")).thenReturn(false);
        when(crewMemberRepository.existsByEmail("new.crew@example.com")).thenReturn(false);
        when(crewMemberMapper.toEntity(request)).thenReturn(mappedEntity);
        when(crewMemberRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(crewMemberMapper.toResponse(savedEntity)).thenReturn(expectedResponse);

        CrewMemberResponse result = crewMemberService.createCrewMember(request);

        assertThat(result).isEqualTo(expectedResponse);
        verify(crewMemberRepository).save(mappedEntity);
    }

    @Test
    void createCrewMember_throwsDuplicateResourceException_whenEmployeeIdAlreadyExists() {
        CrewMemberCreateRequest request = new CrewMemberCreateRequest();
        request.setEmployeeId("EMP-001");
        request.setEmail("new.crew@example.com");

        when(crewMemberRepository.existsByEmployeeId("EMP-001")).thenReturn(true);

        assertThatThrownBy(() -> crewMemberService.createCrewMember(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("EMP-001");

        verify(crewMemberRepository, never()).save(any());
    }

    @Test
    void createCrewMember_throwsDuplicateResourceException_whenEmailAlreadyExists() {
        CrewMemberCreateRequest request = new CrewMemberCreateRequest();
        request.setEmployeeId("EMP-002");
        request.setEmail("alex.chen@example.com");

        when(crewMemberRepository.existsByEmployeeId("EMP-002")).thenReturn(false);
        when(crewMemberRepository.existsByEmail("alex.chen@example.com")).thenReturn(true);

        assertThatThrownBy(() -> crewMemberService.createCrewMember(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("alex.chen@example.com");

        verify(crewMemberRepository, never()).save(any());
    }

    @Test
    void getCrewMemberById_returnsResponse_whenExists() {
        CrewMemberResponse expectedResponse = CrewMemberResponse.builder().id(1L).build();
        when(crewMemberRepository.findById(1L)).thenReturn(Optional.of(existingCrewMember));
        when(crewMemberMapper.toResponse(existingCrewMember)).thenReturn(expectedResponse);

        CrewMemberResponse result = crewMemberService.getCrewMemberById(1L);

        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    void getCrewMemberById_throwsResourceNotFoundException_whenNotExists() {
        when(crewMemberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> crewMemberService.getCrewMemberById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAllCrewMembers_returnsMappedList() {
        CrewMember second = new CrewMember();
        second.setId(2L);
        CrewMemberResponse response1 = CrewMemberResponse.builder().id(1L).build();
        CrewMemberResponse response2 = CrewMemberResponse.builder().id(2L).build();

        when(crewMemberRepository.findAll()).thenReturn(List.of(existingCrewMember, second));
        when(crewMemberMapper.toResponse(existingCrewMember)).thenReturn(response1);
        when(crewMemberMapper.toResponse(second)).thenReturn(response2);

        List<CrewMemberResponse> result = crewMemberService.getAllCrewMembers();

        assertThat(result).containsExactly(response1, response2);
    }

    @Test
    void updateCrewMember_updatesAndReturnsResponse_whenEmployeeIdAndEmailUnchanged() {
        CrewMemberUpdateRequest request = new CrewMemberUpdateRequest();
        request.setEmployeeId(existingCrewMember.getEmployeeId());
        request.setEmail(existingCrewMember.getEmail());

        CrewMemberResponse expectedResponse = CrewMemberResponse.builder().id(1L).build();

        when(crewMemberRepository.findById(1L)).thenReturn(Optional.of(existingCrewMember));
        when(crewMemberRepository.save(existingCrewMember)).thenReturn(existingCrewMember);
        when(crewMemberMapper.toResponse(existingCrewMember)).thenReturn(expectedResponse);

        CrewMemberResponse result = crewMemberService.updateCrewMember(1L, request);

        assertThat(result).isEqualTo(expectedResponse);
        verify(crewMemberMapper).updateEntity(existingCrewMember, request);
        verify(crewMemberRepository, never()).existsByEmployeeId(any());
        verify(crewMemberRepository, never()).existsByEmail(any());
    }

    @Test
    void updateCrewMember_throwsDuplicateResourceException_whenNewEmployeeIdBelongsToAnotherCrewMember() {
        CrewMemberUpdateRequest request = new CrewMemberUpdateRequest();
        request.setEmployeeId("EMP-999");
        request.setEmail(existingCrewMember.getEmail());

        when(crewMemberRepository.findById(1L)).thenReturn(Optional.of(existingCrewMember));
        when(crewMemberRepository.existsByEmployeeId("EMP-999")).thenReturn(true);

        assertThatThrownBy(() -> crewMemberService.updateCrewMember(1L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("EMP-999");

        verify(crewMemberRepository, never()).save(any());
    }

    @Test
    void updateCrewMember_throwsResourceNotFoundException_whenNotExists() {
        CrewMemberUpdateRequest request = new CrewMemberUpdateRequest();
        when(crewMemberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> crewMemberService.updateCrewMember(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(crewMemberRepository, never()).save(any());
    }

    @Test
    void deleteCrewMember_deletesCrewMember_whenExists() {
        when(crewMemberRepository.findById(1L)).thenReturn(Optional.of(existingCrewMember));

        crewMemberService.deleteCrewMember(1L);

        verify(crewMemberRepository).delete(existingCrewMember);
    }

    @Test
    void deleteCrewMember_throwsResourceNotFoundException_whenNotExists() {
        when(crewMemberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> crewMemberService.deleteCrewMember(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(crewMemberRepository, never()).delete(any());
    }

    @Test
    void getAvailableCrew_computesFlightHoursAndWindowStart_andReturnsMappedList() {
        ReflectionTestUtils.setField(crewMemberService, "maxWeeklyHours", 40.0);

        LocalDateTime start = LocalDateTime.of(2026, 3, 10, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 3, 10, 11, 30); // 2.5-hour flight
        LocalDate expectedWindowStart = LocalDate.of(2026, 3, 4); // start's date minus 6 days

        CrewMemberResponse response = CrewMemberResponse.builder().id(1L).build();

        when(crewMemberRepository.findAvailableCrew(eq(start), eq(end), eq(expectedWindowStart), eq(2.5), eq(40.0)))
                .thenReturn(List.of(existingCrewMember));
        when(crewMemberMapper.toResponse(existingCrewMember)).thenReturn(response);

        List<CrewMemberResponse> result = crewMemberService.getAvailableCrew(start, end);

        assertThat(result).containsExactly(response);
    }
}
