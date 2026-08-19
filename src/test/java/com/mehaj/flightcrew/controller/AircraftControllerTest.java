package com.mehaj.flightcrew.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mehaj.flightcrew.dto.AircraftCreateRequest;
import com.mehaj.flightcrew.dto.AircraftResponse;
import com.mehaj.flightcrew.dto.AircraftUpdateRequest;
import com.mehaj.flightcrew.entity.AircraftStatus;
import com.mehaj.flightcrew.exception.DuplicateResourceException;
import com.mehaj.flightcrew.exception.ResourceNotFoundException;
import com.mehaj.flightcrew.service.AircraftService;
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

@WebMvcTest(AircraftController.class)
class AircraftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AircraftService aircraftService;

    private AircraftCreateRequest validCreateRequest() {
        AircraftCreateRequest request = new AircraftCreateRequest();
        request.setTailNumber("N123AB");
        request.setModel("Boeing 737-800");
        request.setCapacity(180);
        return request;
    }

    @Test
    void createAircraft_returns201WithLocationHeader_whenRequestIsValid() throws Exception {
        when(aircraftService.createAircraft(any(AircraftCreateRequest.class)))
                .thenReturn(AircraftResponse.builder().id(1L).status(AircraftStatus.ACTIVE).build());

        mockMvc.perform(post("/api/aircraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/aircraft/1"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createAircraft_returns400_whenCapacityIsNotPositive() throws Exception {
        AircraftCreateRequest request = validCreateRequest();
        request.setCapacity(0);

        mockMvc.perform(post("/api/aircraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[*].field").value(hasItem("capacity")));
    }

    @Test
    void createAircraft_returns409_whenServiceThrowsDuplicateResourceException() throws Exception {
        when(aircraftService.createAircraft(any(AircraftCreateRequest.class)))
                .thenThrow(new DuplicateResourceException("Aircraft with tail number 'N123AB' already exists"));

        mockMvc.perform(post("/api/aircraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void getAircraftById_returns200_whenExists() throws Exception {
        when(aircraftService.getAircraftById(1L)).thenReturn(AircraftResponse.builder().id(1L).build());

        mockMvc.perform(get("/api/aircraft/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAircraftById_returns404_whenNotFound() throws Exception {
        when(aircraftService.getAircraftById(99L))
                .thenThrow(new ResourceNotFoundException("Aircraft not found with id 99"));

        mockMvc.perform(get("/api/aircraft/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getAllAircraft_returns200WithJsonArray() throws Exception {
        when(aircraftService.getAllAircraft()).thenReturn(List.of(
                AircraftResponse.builder().id(1L).build(),
                AircraftResponse.builder().id(2L).build()));

        mockMvc.perform(get("/api/aircraft"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateAircraft_returns200_whenValid() throws Exception {
        AircraftUpdateRequest request = new AircraftUpdateRequest();
        request.setTailNumber("N123AB");
        request.setModel("Boeing 737-800");
        request.setCapacity(180);
        request.setStatus(AircraftStatus.ACTIVE);

        when(aircraftService.updateAircraft(eq(1L), any(AircraftUpdateRequest.class)))
                .thenReturn(AircraftResponse.builder().id(1L).build());

        mockMvc.perform(put("/api/aircraft/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteAircraft_returns204_whenExists() throws Exception {
        mockMvc.perform(delete("/api/aircraft/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(aircraftService).deleteAircraft(1L);
    }
}
