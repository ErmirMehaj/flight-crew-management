package com.mehaj.flightcrew.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mehaj.flightcrew.dto.PilotCreateRequest;
import com.mehaj.flightcrew.dto.PilotResponse;
import com.mehaj.flightcrew.dto.PilotUpdateRequest;
import com.mehaj.flightcrew.entity.PilotRank;
import com.mehaj.flightcrew.entity.PilotStatus;
import com.mehaj.flightcrew.exception.DuplicateResourceException;
import com.mehaj.flightcrew.exception.ResourceNotFoundException;
import com.mehaj.flightcrew.service.PilotService;
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

@WebMvcTest(PilotController.class)
class PilotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PilotService pilotService;

    private PilotCreateRequest validCreateRequest() {
        PilotCreateRequest request = new PilotCreateRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setLicenseNumber("LIC-001");
        request.setEmail("jane.doe@example.com");
        request.setRank(PilotRank.CAPTAIN);
        return request;
    }

    @Test
    void createPilot_returns201WithLocationHeader_whenRequestIsValid() throws Exception {
        PilotResponse response = PilotResponse.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Doe")
                .licenseNumber("LIC-001")
                .email("jane.doe@example.com")
                .rank(PilotRank.CAPTAIN)
                .status(PilotStatus.ACTIVE)
                .totalFlightHours(0.0)
                .build();
        when(pilotService.createPilot(any(PilotCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/pilots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/pilots/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createPilot_returns400_whenRequiredFieldsAreMissing() throws Exception {
        PilotCreateRequest request = new PilotCreateRequest(); // every field blank/null

        mockMvc.perform(post("/api/pilots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("firstName")));
    }

    @Test
    void createPilot_returns409_whenServiceThrowsDuplicateResourceException() throws Exception {
        when(pilotService.createPilot(any(PilotCreateRequest.class)))
                .thenThrow(new DuplicateResourceException("Pilot with license number 'LIC-001' already exists"));

        mockMvc.perform(post("/api/pilots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Pilot with license number 'LIC-001' already exists"));
    }

    @Test
    void getPilotById_returns200_whenExists() throws Exception {
        PilotResponse response = PilotResponse.builder().id(1L).firstName("Jane").build();
        when(pilotService.getPilotById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/pilots/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    void getPilotById_returns404_whenNotFound() throws Exception {
        when(pilotService.getPilotById(99L))
                .thenThrow(new ResourceNotFoundException("Pilot not found with id 99"));

        mockMvc.perform(get("/api/pilots/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Pilot not found with id 99"));
    }

    @Test
    void getAllPilots_returns200WithJsonArray() throws Exception {
        PilotResponse first = PilotResponse.builder().id(1L).build();
        PilotResponse second = PilotResponse.builder().id(2L).build();
        when(pilotService.getAllPilots()).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/pilots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void updatePilot_returns200_whenValid() throws Exception {
        PilotUpdateRequest request = new PilotUpdateRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setLicenseNumber("LIC-001");
        request.setEmail("jane.doe@example.com");
        request.setRank(PilotRank.CAPTAIN);
        request.setStatus(PilotStatus.ACTIVE);

        PilotResponse response = PilotResponse.builder().id(1L).firstName("Jane").build();
        when(pilotService.updatePilot(eq(1L), any(PilotUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/pilots/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deletePilot_returns204_whenExists() throws Exception {
        mockMvc.perform(delete("/api/pilots/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(pilotService).deletePilot(1L);
    }

    @Test
    void unknownRoute_returns404_notSwallowedAs500ByTheCatchAllHandler() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
