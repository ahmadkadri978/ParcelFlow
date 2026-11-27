package com.parcelflow.shipment;

import com.parcelflow.support.PostgresTestConfiguration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "DB_PASSWORD=unused")
@Import(PostgresTestConfiguration.class)
class ShipmentEventIntegrationTest {

    @Autowired
    private ShipmentService shipmentService;

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
    void shouldRecordShipmentCreationEvent() {

        Shipment shipment = shipmentService.createShipment(
                "John Smith",
                "Berlin",
                "Hamburg"
        );

        List<ShipmentEvent> events =
                shipmentEventRepository
                        .findByShipment_IdOrderBySequenceNumberAsc(
                                shipment.getId()
                        );

        assertEquals(1, events.size());

        ShipmentEvent event = events.get(0);

        assertEquals(
                ShipmentEventType.CREATED,
                event.getEventType()
        );

        assertNull(event.getPreviousStatus());

        assertEquals(
                ShipmentStatus.CREATED,
                event.getNewStatus()
        );

        assertEquals(0L, event.getSequenceNumber());
        assertNotNull(event.getId());
        assertNotNull(event.getOccurredAt());
    }

    @Test
    void shouldRecordEventsInCorrectSequence() {

        Shipment shipment = shipmentService.createShipment(
                "Anna Miller",
                "Munich",
                "Berlin"
        );

        UUID shipmentId = shipment.getId();

        shipmentService.updateShipmentStatus(
                shipmentId,
                ShipmentStatus.PICKED_UP
        );

        shipmentService.updateShipmentStatus(
                shipmentId,
                ShipmentStatus.IN_TRANSIT
        );

        List<ShipmentEvent> events =
                shipmentEventRepository
                        .findByShipment_IdOrderBySequenceNumberAsc(
                                shipmentId
                        );

        assertEquals(3, events.size());

        assertEquals(0L, events.get(0).getSequenceNumber());
        assertEquals(1L, events.get(1).getSequenceNumber());
        assertEquals(2L, events.get(2).getSequenceNumber());

        assertEquals(
                ShipmentEventType.STATUS_CHANGED,
                events.get(1).getEventType()
        );

        assertEquals(
                ShipmentStatus.CREATED,
                events.get(1).getPreviousStatus()
        );

        assertEquals(
                ShipmentStatus.PICKED_UP,
                events.get(1).getNewStatus()
        );

        assertEquals(
                ShipmentStatus.PICKED_UP,
                events.get(2).getPreviousStatus()
        );

        assertEquals(
                ShipmentStatus.IN_TRANSIT,
                events.get(2).getNewStatus()
        );

        Shipment currentShipment = shipmentRepository
                .findById(shipmentId)
                .orElseThrow();

        assertEquals(
                ShipmentStatus.IN_TRANSIT,
                currentShipment.getStatus()
        );

        assertEquals(
                2L,
                currentShipment.getVersion().longValue()
        );
    }

    @Test
    void shouldNotRecordEventForInvalidTransition() {

        Shipment shipment = shipmentService.createShipment(
                "David Brown",
                "Hamburg",
                "Cologne"
        );

        UUID shipmentId = shipment.getId();

        assertThrows(
                InvalidShipmentStatusTransitionException.class,
                () -> shipmentService.updateShipmentStatus(
                        shipmentId,
                        ShipmentStatus.DELIVERED
                )
        );

        List<ShipmentEvent> events =
                shipmentEventRepository
                        .findByShipment_IdOrderBySequenceNumberAsc(
                                shipmentId
                        );

        assertEquals(1, events.size());

        assertEquals(
                ShipmentEventType.CREATED,
                events.get(0).getEventType()
        );

        Shipment unchanged = shipmentRepository
                .findById(shipmentId)
                .orElseThrow();

        assertEquals(
                ShipmentStatus.CREATED,
                unchanged.getStatus()
        );

        assertEquals(
                0L,
                unchanged.getVersion().longValue()
        );
    }
}
