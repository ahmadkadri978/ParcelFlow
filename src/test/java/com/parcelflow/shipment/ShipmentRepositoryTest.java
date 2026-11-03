package com.parcelflow.shipment;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

@SpringBootTest
@Transactional
public class ShipmentRepositoryTest {
    @Autowired
    ShipmentRepository shipmentRepository;
    @PersistenceContext
    private EntityManager entityManager;
    @Test
     void shouldSaveAndFindShipment() {
        String trackingNumber = "PF-" + UUID.randomUUID();
        Shipment shipment = new Shipment(
                trackingNumber,
                "John Smith",
                "Berlin",
                "Hamburg"
        );
        shipmentRepository.saveAndFlush(shipment);
        entityManager.clear();
        Shipment savedShipment = shipmentRepository
                .findByTrackingNumber(trackingNumber)
                .orElseThrow();

        assertNotNull(savedShipment.getId());

        assertEquals(
                ShipmentStatus.CREATED,
                savedShipment.getStatus()
        );
        assertEquals(
                "Berlin",
                savedShipment.getOrigin()
        );

        assertEquals(
                "Hamburg",
                savedShipment.getDestination()
        );

        assertNotNull(savedShipment.getCreatedAt());

    }

    @Test
    void shouldReturnEmptyWhenShipmentDoesNotExist() {

        boolean exists = shipmentRepository
                .existsByTrackingNumber("UNKNOWN-123");

        assertFalse(exists);
    }
}
