package com.mehaj.flightcrew.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mehaj.flightcrew.dto.CrewMemberCreateRequest;
import com.mehaj.flightcrew.dto.CrewMemberResponse;
import com.mehaj.flightcrew.dto.CrewMemberUpdateRequest;
import com.mehaj.flightcrew.entity.CrewPosition;
import com.mehaj.flightcrew.entity.CrewStatus;
import com.mehaj.flightcrew.exception.DuplicateResourceException;
import com.mehaj.flightcrew.exception.ResourceNotFoundException;
import com.mehaj.flightcrew.service.CrewMemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CrewMemberController.class)
class CrewMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CrewMemberService crewMemberService;

    private CrewMemberCreateRequest validCreateRequest() {
        CrewMemberCreateRequest request = new CrewMemberCreateRequest();
        request.setFirstName("Alex");
        request.setLastName("Chen");
        request.setEmployeeId("EMP-001");
        request.setEmail("alex.chen@example.com");
        request.setPosition(CrewPosition.PURSER);
        return request;
    }

    @Test
    void createCrewMember_returns201WithLocationHeader_whenRequestIsValid() throws Exception {
        CrewMemberResponse response = CrewMemberResponse.builder()
                .id(1L).firstName("Alex").status(CrewStatus.ACTIVE).build();
        when(crewMemberService.createCrewMember(any(CrewMemberCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/crew-members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/crew-members/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createCrewMember_returns400_whenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/api/crew-members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CrewMemberCreateRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("employeeId")));
    }

    @Test
    void createCrewMember_returns409_whenServiceThrowsDuplicateResourceException() throws Exception {
        when(crewMemberService.createCrewMember(any(CrewMemberCreateRequest.class)))
                .thenThrow(new DuplicateResourceException("Crew member with employee ID 'EMP-001' already exists"));

        mockMvc.perform(post("/api/crew-members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void getCrewMemberById_returns200_whenExists() throws Exception {
        when(crewMemberService.getCrewMemberById(1L))
                .thenReturn(CrewMemberResponse.builder().id(1L).firstName("Alex").build());

        mockMvc.perform(get("/api/crew-members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Alex"));
    }

    @Test
    void getCrewMemberById_returns404_whenNotFound() throws Exception {
        when(crewMemberService.getCrewMemberById(99L))
                .thenThrow(new ResourceNotFoundException("Crew member not found with id 99"));

        mockMvc.perform(get("/api/crew-members/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getAllCrewMembers_returns200WithJsonArray() throws Exception {
        when(crewMemberService.getAllCrewMembers()).thenReturn(List.of(
                CrewMemberResponse.builder().id(1L).build(),
                CrewMemberResponse.builder().id(2L).build()));

        mockMvc.perform(get("/api/crew-members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateCrewMember_returns200_whenValid() throws Exception {
        CrewMemberUpdateRequest request = new CrewMemberUpdateRequest();
        request.setFirstName("Alex");
        request.setLastName("Chen");
        request.setEmployeeId("EMP-001");
        request.setEmail("alex.chen@example.com");
        request.setPosition(CrewPosition.PURSER);
        request.setStatus(CrewStatus.ACTIVE);

        when(crewMemberService.updateCrewMember(eq(1L), any(CrewMemberUpdateRequest.class)))
                .thenReturn(CrewMemberResponse.builder().id(1L).build());

        mockMvc.perform(put("/api/crew-members/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteCrewMember_returns204_whenExists() throws Exception {
        mockMvc.perform(delete("/api/crew-members/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(crewMemberService).deleteCrewMember(1L);
    }
}
