package com.parcelflow.shipment.api;

import com.parcelflow.shipment.Shipment;
import com.parcelflow.shipment.ShipmentRepository;
import com.parcelflow.shipment.ShipmentStatus;
import com.parcelflow.support.PostgresTestConfiguration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.parcelflow.shipment.ShipmentEventRepository;

@SpringBootTest(properties = "DB_PASSWORD=unused")
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
class ShipmentApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShipmentRepository shipmentRepository;
    @Autowired
    private ShipmentEventRepository shipmentEventRepository;

    @AfterEach
    void cleanUp() {

        shipmentEventRepository.deleteAllInBatch();

        shipmentRepository.deleteAllInBatch();
    }

    @Test
    void shouldCreateAndRetrieveShipment() throws Exception {

        String requestBody = """
                {
                  "recipientName": "John Smith",
                  "origin": "Berlin",
                  "destination": "Hamburg"
                }
                """;

        // Step 1: Create a shipment through HTTP
        MvcResult result = mockMvc.perform(
                        post("/api/v1/shipments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andReturn();

        // Step 2: Read the Location response header
        String location = result.getResponse()
                .getHeader("Location");

        assertNotNull(location);

        String idValue = location.substring(
                location.lastIndexOf('/') + 1
        );

        UUID shipmentId = UUID.fromString(idValue);

        // Step 3: Verify the shipment exists in PostgreSQL
        Shipment savedShipment = shipmentRepository
                .findById(shipmentId)
                .orElseThrow();

        assertEquals(
                "John Smith",
                savedShipment.getRecipientName()
        );

        assertEquals(
                "Berlin",
                savedShipment.getOrigin()
        );

        assertNotNull(savedShipment.getCreatedAt());

        // Step 4: Retrieve the shipment through HTTP
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(shipmentId.toString()))
                .andExpect(jsonPath("$.destination")
                        .value("Hamburg"));

        // Step 5: Retrieve using the tracking number
        mockMvc.perform(
                        get(
                                "/api/v1/shipments/track/{trackingNumber}",
                                savedShipment.getTrackingNumber()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(shipmentId.toString()));
    }

    @Test
    void shouldUpdateStatusAndRejectInvalidTransition()
            throws Exception {

        // Create a shipment in PostgreSQL
        Shipment shipment = new Shipment(
                "PF-" + UUID.randomUUID(),
                "John Smith",
                "Berlin",
                "Hamburg"
        );

        Shipment saved = shipmentRepository.saveAndFlush(shipment);

        UUID id = saved.getId();
        long initialVersion = saved.getVersion();

        // Update CREATED -> PICKED_UP through the API
        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {"status": "PICKED_UP"}
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("PICKED_UP"));

        // Verify the change was saved in PostgreSQL
        Shipment updated = shipmentRepository
                .findById(id)
                .orElseThrow();

        assertEquals(
                ShipmentStatus.PICKED_UP,
                updated.getStatus()
        );

        assertEquals(
                initialVersion + 1,
                updated.getVersion().longValue()
        );

        // Trying an invalid transition: PICKED_UP -> DELIVERED
        mockMvc.perform(
                        patch("/api/v1/shipments/{id}/status", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {"status": "DELIVERED"}
                                    """)
                )
                .andExpect(status().isConflict());

        // Verify the invalid transition changed nothing
        Shipment unchanged = shipmentRepository
                .findById(id)
                .orElseThrow();

        assertEquals(
                ShipmentStatus.PICKED_UP,
                unchanged.getStatus()
        );

        assertEquals(
                initialVersion + 1,
                unchanged.getVersion().longValue()
        );
    }
}