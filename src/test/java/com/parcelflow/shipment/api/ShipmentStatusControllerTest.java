
package com.parcelflow.shipment.api;

import com.parcelflow.shipment.*;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShipmentController.class)
class ShipmentStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShipmentService shipmentService;

    @Test
    void shouldUpdateShipmentStatus() throws Exception {

        UUID id = UUID.randomUUID();

        Shipment shipment = mock(Shipment.class);

        when(shipment.getId()).thenReturn(id);
        when(shipment.getTrackingNumber()).thenReturn("PF-123");
        when(shipment.getRecipientName()).thenReturn("John Smith");
        when(shipment.getOrigin()).thenReturn("Berlin");
        when(shipment.getDestination()).thenReturn("Hamburg");
        when(shipment.getStatus()).thenReturn(ShipmentStatus.PICKED_UP);
        when(shipment.getCreatedAt())
                .thenReturn(Instant.parse("2026-11-26T12:00:00Z"));

        when(shipmentService.updateShipmentStatus(
                id,
                ShipmentStatus.PICKED_UP
        )).thenReturn(shipment);

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"status": "PICKED_UP"}
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("PICKED_UP"));

        verify(shipmentService).updateShipmentStatus(
                id,
                ShipmentStatus.PICKED_UP
        );
    }

    @Test
    void shouldReturn409ForInvalidTransition() throws Exception {

        UUID id = UUID.randomUUID();

        when(shipmentService.updateShipmentStatus(
                id,
                ShipmentStatus.DELIVERED
        )).thenThrow(
                new InvalidShipmentStatusTransitionException(
                        ShipmentStatus.CREATED,
                        ShipmentStatus.DELIVERED
                )
        );

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"status": "DELIVERED"}
                                        """)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Cannot change shipment status from CREATED to DELIVERED"
                ));
    }

    @Test
    void shouldReturn404ForUnknownShipment() throws Exception {

        UUID id = UUID.randomUUID();

        when(shipmentService.updateShipmentStatus(
                id,
                ShipmentStatus.PICKED_UP
        )).thenThrow(
                new ShipmentNotFoundException("Shipment not found")
        );

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"status": "PICKED_UP"}
                                        """)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Shipment not found"));
    }

    @Test
    void shouldRejectMissingStatus() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.fieldErrors.status")
                        .value("Status is required"));

        verifyNoInteractions(shipmentService);
    }

    @Test
    void shouldRejectUnsupportedStatus() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"status": "SHIPPED"}
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Invalid request body"));

        verifyNoInteractions(shipmentService);
    }
}
