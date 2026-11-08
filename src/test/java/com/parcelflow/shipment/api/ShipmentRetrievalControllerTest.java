package com.parcelflow.shipment.api;

import com.parcelflow.shipment.*;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShipmentController.class)
class ShipmentRetrievalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShipmentService shipmentService;

    @Test
    void shouldFindShipmentById() throws Exception {

        UUID id = UUID.randomUUID();
        Shipment shipment = createMockShipment(id);

        when(shipmentService.getShipmentById(id))
                .thenReturn(shipment);

        mockMvc.perform(get("/api/v1/shipments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(id.toString()))
                .andExpect(jsonPath("$.recipientName")
                        .value("John Smith"))
                .andExpect(jsonPath("$.status")
                        .value("CREATED"));

        verify(shipmentService).getShipmentById(id);
    }

    @Test
    void shouldReturn404WhenShipmentDoesNotExist()
            throws Exception {

        UUID id = UUID.randomUUID();

        when(shipmentService.getShipmentById(id))
                .thenThrow(
                        new ShipmentNotFoundException(
                                "Shipment not found with ID: " + id
                        )
                );

        mockMvc.perform(get("/api/v1/shipments/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Shipment not found with ID: " + id));
    }

    @Test
    void shouldFindShipmentByTrackingNumber()
            throws Exception {

        UUID id = UUID.randomUUID();
        Shipment shipment = createMockShipment(id);

        String trackingNumber = shipment.getTrackingNumber();

        when(shipmentService.getShipmentByTrackingNumber(trackingNumber))
                .thenReturn(shipment);

        mockMvc.perform(
                        get("/api/v1/shipments/track/{trackingNumber}",
                                trackingNumber)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(id.toString()))
                .andExpect(jsonPath("$.trackingNumber")
                        .value(trackingNumber));

        verify(shipmentService)
                .getShipmentByTrackingNumber(trackingNumber);
    }

    @Test
    void shouldReturn404ForUnknownTrackingNumber()
            throws Exception {

        String trackingNumber = "PF-UNKNOWN";

        when(shipmentService.getShipmentByTrackingNumber(trackingNumber))
                .thenThrow(
                        new ShipmentNotFoundException(
                                "Shipment not found"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/shipments/track/{trackingNumber}",
                                trackingNumber)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Shipment not found"));
    }

    @Test
    void shouldReturnPaginatedShipments()
            throws Exception {

        UUID id = UUID.randomUUID();
        Shipment shipment = createMockShipment(id);

        when(shipmentService.getShipments(0, 10))
                .thenReturn(
                        new PageImpl<>(
                                List.of(shipment),
                                PageRequest.of(0, 10),
                                1
                        )
                );

        mockMvc.perform(
                        get("/api/v1/shipments")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id")
                        .value(id.toString()))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));

        verify(shipmentService).getShipments(0, 10);
    }

    @Test
    void shouldRejectInvalidPageSize()
            throws Exception {

        when(shipmentService.getShipments(0, 101))
                .thenThrow(
                        new IllegalArgumentException(
                                "Page size must be between 1 and 100"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/shipments")
                                .param("page", "0")
                                .param("size", "101")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Page size must be between 1 and 100"));
    }

    @Test
    void shouldRejectInvalidUUID()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/shipments/not-a-uuid")
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(shipmentService);
    }

    private Shipment createMockShipment(UUID id) {

        Shipment shipment = mock(Shipment.class);

        when(shipment.getId()).thenReturn(id);

        when(shipment.getTrackingNumber())
                .thenReturn("PF-550e8400-e29b-41d4-a716-446655440001");

        when(shipment.getRecipientName())
                .thenReturn("John Smith");

        when(shipment.getOrigin())
                .thenReturn("Berlin");

        when(shipment.getDestination())
                .thenReturn("Hamburg");

        when(shipment.getStatus())
                .thenReturn(ShipmentStatus.CREATED);

        when(shipment.getCreatedAt())
                .thenReturn(Instant.parse("2026-11-07T12:00:00Z"));

        return shipment;
    }
}
