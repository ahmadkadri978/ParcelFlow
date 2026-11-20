package com.parcelflow.shipment;


import com.parcelflow.support.PostgresTestConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

@SpringBootTest(properties = "DB_PASSWORD=unused")
@Import(PostgresTestConfiguration.class)
@Transactional
 class ShipmentRepositoryTest {
    @Autowired
    ShipmentRepository shipmentRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldRejectDuplicateTrackingNumber() {

        String trackingNumber = "PF-" + UUID.randomUUID();

        Shipment firstShipment = new Shipment(
                trackingNumber,
                "John Smith",
                "Berlin",
                "Hamburg"
        );

        shipmentRepository.saveAndFlush(firstShipment);

        Shipment secondShipment = new Shipment(
                trackingNumber,
                "Anna Miller",
                "Munich",
                "Cologne"
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> shipmentRepository.saveAndFlush(secondShipment)
        );
    }

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
    void shouldReturnPaginatedShipments() {

        for (int i = 0; i < 3; i++) {

            Shipment shipment = new Shipment(
                    "PF-" + UUID.randomUUID(),
                    "Customer " + i,
                    "Berlin",
                    "Hamburg"
            );

            shipmentRepository.save(shipment);
        }

        shipmentRepository.flush();

        PageRequest pageRequest = PageRequest.of(
                0,
                2,
                Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")
                )
        );

        Page<Shipment> result =
                shipmentRepository.findAll(pageRequest);

        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void shouldReturnEmptyWhenShipmentDoesNotExist() {

        boolean exists = shipmentRepository
                .existsByTrackingNumber("UNKNOWN-123");

        assertFalse(exists);
    }
}
