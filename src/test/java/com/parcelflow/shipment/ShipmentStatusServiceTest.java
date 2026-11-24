package com.parcelflow.shipment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentStatusServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    private ShipmentService shipmentService;

    @BeforeEach
    void setUp() {
        shipmentService = new ShipmentService(shipmentRepository);
    }

    @Test
    void shouldUpdateShipmentStatus() {

        UUID shipmentId = UUID.randomUUID();

        Shipment shipment = new Shipment(
                "PF-123",
                "John Smith",
                "Berlin",
                "Hamburg"
        );

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.of(shipment));

        Shipment result = shipmentService.updateShipmentStatus(
                shipmentId,
                ShipmentStatus.PICKED_UP
        );

        assertSame(shipment, result);
        assertEquals(ShipmentStatus.PICKED_UP, result.getStatus());

        verify(shipmentRepository).findById(shipmentId);
    }

    @Test
    void shouldRejectInvalidTransition() {

        UUID shipmentId = UUID.randomUUID();

        Shipment shipment = new Shipment(
                "PF-123",
                "John Smith",
                "Berlin",
                "Hamburg"
        );

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.of(shipment));

        assertThrows(
                InvalidShipmentStatusTransitionException.class,
                () -> shipmentService.updateShipmentStatus(
                        shipmentId,
                        ShipmentStatus.DELIVERED
                )
        );

        assertEquals(ShipmentStatus.CREATED, shipment.getStatus());
    }

    @Test
    void shouldRejectUnknownShipment() {

        UUID shipmentId = UUID.randomUUID();

        when(shipmentRepository.findById(shipmentId))
                .thenReturn(Optional.empty());

        assertThrows(
                ShipmentNotFoundException.class,
                () -> shipmentService.updateShipmentStatus(
                        shipmentId,
                        ShipmentStatus.PICKED_UP
                )
        );

        verify(shipmentRepository).findById(shipmentId);
    }
}
