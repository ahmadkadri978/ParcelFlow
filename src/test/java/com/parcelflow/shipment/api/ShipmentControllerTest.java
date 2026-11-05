package com.parcelflow.shipment.api;

import com.parcelflow.shipment.Shipment;
import com.parcelflow.shipment.ShipmentService;
import com.parcelflow.shipment.ShipmentStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShipmentController.class)
class ShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShipmentService shipmentService;

    @Test
    void shouldCreateShipment() throws Exception {

        UUID id = UUID.fromString(
                "550e8400-e29b-41d4-a716-446655440000"
        );

        Shipment savedShipment = mock(Shipment.class);

        when(savedShipment.getId()).thenReturn(id);

        when(savedShipment.getTrackingNumber())
                .thenReturn("PF-550e8400-e29b-41d4-a716-446655440001");

        when(savedShipment.getRecipientName())
                .thenReturn("John Smith");

        when(savedShipment.getOrigin())
                .thenReturn("Berlin");

        when(savedShipment.getDestination())
                .thenReturn("Hamburg");

        when(savedShipment.getStatus())
                .thenReturn(ShipmentStatus.CREATED);

        when(savedShipment.getCreatedAt())
                .thenReturn(Instant.parse("2026-11-05T12:00:00Z"));

        when(shipmentService.createShipment(
                "John Smith",
                "Berlin",
                "Hamburg"
        )).thenReturn(savedShipment);

        String requestBody = """
                {
                  "recipientName": "John Smith",
                  "origin": "Berlin",
                  "destination": "Hamburg"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/shipments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "/api/v1/shipments/" + id
                ))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.recipientName").value("John Smith"))
                .andExpect(jsonPath("$.origin").value("Berlin"))
                .andExpect(jsonPath("$.destination").value("Hamburg"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.createdAt").exists());

        verify(shipmentService).createShipment(
                "John Smith",
                "Berlin",
                "Hamburg"
        );
    }

    @Test
    void shouldRejectBlankRecipientName() throws Exception {

        String requestBody = """
                {
                  "recipientName": " ",
                  "origin": "Berlin",
                  "destination": "Hamburg"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/shipments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.recipientName")
                        .value("Recipient name is required"));

        verifyNoInteractions(shipmentService);
    }

    @Test
    void shouldRejectTooLongOrigin() throws Exception {

        String requestBody = """
                {
                  "recipientName": "John Smith",
                  "origin": "%s",
                  "destination": "Hamburg"
                }
                """.formatted("A".repeat(121));

        mockMvc.perform(
                        post("/api/v1/shipments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.origin").exists());

        verifyNoInteractions(shipmentService);
    }

    @Test
    void shouldRejectMissingDestination() throws Exception {

        String requestBody = """
                {
                  "recipientName": "John Smith",
                  "origin": "Berlin"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/shipments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.destination")
                        .value("Destination is required"));

        verifyNoInteractions(shipmentService);
    }

    @Test
    void shouldHandleServiceValidationError() throws Exception {

        String requestBody = """
                {
                  "recipientName": "John Smith",
                  "origin": "Berlin",
                  "destination": "Hamburg"
                }
                """;

        when(shipmentService.createShipment(
                "John Smith",
                "Berlin",
                "Hamburg"
        )).thenThrow(
                new IllegalArgumentException("Invalid shipment data")
        );

        mockMvc.perform(
                        post("/api/v1/shipments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Invalid shipment data"));
    }
}
