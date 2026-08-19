package com.mehaj.flightcrew.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mehaj.flightcrew.dto.FlightCreateRequest;
import com.mehaj.flightcrew.dto.FlightResponse;
import com.mehaj.flightcrew.entity.FlightStatus;
import com.mehaj.flightcrew.entity.PilotRank;
import com.mehaj.flightcrew.exception.ResourceNotFoundException;
import com.mehaj.flightcrew.exception.SchedulingConflictException;
import com.mehaj.flightcrew.service.FlightAssignmentService;
import com.mehaj.flightcrew.service.FlightService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FlightController.class)
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FlightService flightService;

    @MockitoBean
    private FlightAssignmentService flightAssignmentService;

    private FlightCreateRequest validCreateRequest() {
        FlightCreateRequest request = new FlightCreateRequest();
        request.setFlightNumber("FC100");
        request.setDepartureAirport("JFK");
        request.setArrivalAirport("LAX");
        request.setDepartureTime(LocalDateTime.of(2026, 4, 1, 9, 0));
        request.setArrivalTime(LocalDateTime.of(2026, 4, 1, 12, 0));
        return request;
    }

    @Test
    void createFlight_returns201WithLocationHeader_whenValid() throws Exception {
        when(flightService.createFlight(any())).thenReturn(
                FlightResponse.builder().id(1L).status(FlightStatus.SCHEDULED).build());

        mockMvc.perform(post("/api/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/flights/1"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void createFlight_returns400_whenArrivalIsBeforeDeparture() throws Exception {
        FlightCreateRequest request = validCreateRequest();
        request.setArrivalTime(request.getDepartureTime().minusHours(1)); // arrival before departure

        mockMvc.perform(post("/api/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getFlightById_returns200_whenExists() throws Exception {
        when(flightService.getFlightById(1L)).thenReturn(FlightResponse.builder().id(1L).build());

        mockMvc.perform(get("/api/flights/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getFlightById_returns404_whenNotFound() throws Exception {
        when(flightService.getFlightById(99L))
                .thenThrow(new ResourceNotFoundException("Flight not found with id 99"));

        mockMvc.perform(get("/api/flights/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deleteFlight_returns204_whenExists() throws Exception {
        mockMvc.perform(delete("/api/flights/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void assignPilot_returns200_whenSuccessful() throws Exception {
        when(flightAssignmentService.assignPilot(eq(1L), eq(20L), eq(PilotRank.CAPTAIN)))
                .thenReturn(FlightResponse.builder().id(1L).build());

        mockMvc.perform(post("/api/flights/1/pilots/20").param("role", "CAPTAIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void assignPilot_returns409_whenSchedulingConflictExceptionThrown() throws Exception {
        when(flightAssignmentService.assignPilot(eq(1L), eq(20L), eq(PilotRank.CAPTAIN)))
                .thenThrow(new SchedulingConflictException("Pilot LIC-001 is unavailable during this flight's schedule"));

        mockMvc.perform(post("/api/flights/1/pilots/20").param("role", "CAPTAIN"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void completeFlight_returns200_whenSuccessful() throws Exception {
        when(flightAssignmentService.completeFlight(1L))
                .thenReturn(FlightResponse.builder().id(1L).status(FlightStatus.COMPLETED).build());

        mockMvc.perform(post("/api/flights/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
