package com.parcelflow.shipment;

import com.parcelflow.support.PostgresTestConfiguration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.parcelflow.shipment.ShipmentEventRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "DB_PASSWORD=unused")
@Import(PostgresTestConfiguration.class)
class ShipmentStatusIntegrationTest {

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private ShipmentService shipmentService;
    @Autowired
    private ShipmentEventRepository shipmentEventRepository;

    @AfterEach
    void cleanUp() {

        shipmentEventRepository.deleteAllInBatch();

        shipmentRepository.deleteAllInBatch();
    }

    @Test
    void shouldPersistStatusChangeAndIncrementVersion() {

        Shipment shipment = new Shipment(
                "PF-" + UUID.randomUUID(),
                "John Smith",
                "Berlin",
                "Hamburg"
        );

        Shipment saved = shipmentRepository.saveAndFlush(shipment);

        UUID shipmentId = saved.getId();
        long initialVersion = saved.getVersion();

        shipmentService.updateShipmentStatus(
                shipmentId,
                ShipmentStatus.PICKED_UP
        );

        Shipment updated = shipmentRepository
                .findById(shipmentId)
                .orElseThrow();

        assertEquals(
                ShipmentStatus.PICKED_UP,
                updated.getStatus()
        );

        assertEquals(
                initialVersion + 1,
                updated.getVersion().longValue()
        );

        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void shouldNotPersistInvalidTransition() {

        Shipment shipment = new Shipment(
                "PF-" + UUID.randomUUID(),
                "Anna Miller",
                "Munich",
                "Hamburg"
        );

        Shipment saved = shipmentRepository.saveAndFlush(shipment);

        UUID shipmentId = saved.getId();
        long initialVersion = saved.getVersion();

        assertThrows(
                InvalidShipmentStatusTransitionException.class,
                () -> shipmentService.updateShipmentStatus(
                        shipmentId,
                        ShipmentStatus.DELIVERED
                )
        );

        Shipment unchanged = shipmentRepository
                .findById(shipmentId)
                .orElseThrow();

        assertEquals(
                ShipmentStatus.CREATED,
                unchanged.getStatus()
        );

        assertEquals(
                initialVersion,
                unchanged.getVersion().longValue()
        );
    }
}
