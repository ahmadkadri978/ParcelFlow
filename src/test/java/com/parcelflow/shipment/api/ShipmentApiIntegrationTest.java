package com.parcelflow.shipment.api;

import com.parcelflow.shipment.Shipment;
import com.parcelflow.shipment.ShipmentRepository;
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

@SpringBootTest(properties = "DB_PASSWORD=unused")
@AutoConfigureMockMvc
@Import(PostgresTestConfiguration.class)
class ShipmentApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @AfterEach
    void cleanUp() {
        shipmentRepository.deleteAll();
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
}