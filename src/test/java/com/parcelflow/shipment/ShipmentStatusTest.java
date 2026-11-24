package com.parcelflow.shipment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShipmentStatusTest {

    private Shipment newShipment() {
        return new Shipment(
                "PF-123",
                "John Smith",
                "Berlin",
                "Hamburg"
        );
    }

    @Test
    void shouldAllowCompleteShipmentLifecycle() {

        Shipment shipment = newShipment();

        shipment.transitionTo(ShipmentStatus.PICKED_UP);
        assertEquals(ShipmentStatus.PICKED_UP, shipment.getStatus());

        shipment.transitionTo(ShipmentStatus.IN_TRANSIT);
        assertEquals(ShipmentStatus.IN_TRANSIT, shipment.getStatus());

        shipment.transitionTo(ShipmentStatus.OUT_FOR_DELIVERY);
        assertEquals(
                ShipmentStatus.OUT_FOR_DELIVERY,
                shipment.getStatus()
        );

        shipment.transitionTo(ShipmentStatus.DELIVERED);
        assertEquals(ShipmentStatus.DELIVERED, shipment.getStatus());
    }

    @Test
    void shouldRejectSkippingStatuses() {

        Shipment shipment = newShipment();

        assertThrows(
                InvalidShipmentStatusTransitionException.class,
                () -> shipment.transitionTo(ShipmentStatus.DELIVERED)
        );

        assertEquals(ShipmentStatus.CREATED, shipment.getStatus());
    }

    @Test
    void shouldRejectBackwardTransition() {

        Shipment shipment = newShipment();

        shipment.transitionTo(ShipmentStatus.PICKED_UP);
        shipment.transitionTo(ShipmentStatus.IN_TRANSIT);

        assertThrows(
                InvalidShipmentStatusTransitionException.class,
                () -> shipment.transitionTo(ShipmentStatus.PICKED_UP)
        );

        assertEquals(ShipmentStatus.IN_TRANSIT, shipment.getStatus());
    }

    @Test
    void shouldRejectRepeatedStatus() {

        Shipment shipment = newShipment();

        assertThrows(
                InvalidShipmentStatusTransitionException.class,
                () -> shipment.transitionTo(ShipmentStatus.CREATED)
        );

        assertEquals(ShipmentStatus.CREATED, shipment.getStatus());
    }

    @Test
    void shouldRejectTransitionAfterDelivery() {

        Shipment shipment = newShipment();

        shipment.transitionTo(ShipmentStatus.PICKED_UP);
        shipment.transitionTo(ShipmentStatus.IN_TRANSIT);
        shipment.transitionTo(ShipmentStatus.OUT_FOR_DELIVERY);
        shipment.transitionTo(ShipmentStatus.DELIVERED);

        assertThrows(
                InvalidShipmentStatusTransitionException.class,
                () -> shipment.transitionTo(ShipmentStatus.IN_TRANSIT)
        );

        assertEquals(ShipmentStatus.DELIVERED, shipment.getStatus());
    }

    @Test
    void shouldRejectNullStatus() {

        Shipment shipment = newShipment();

        assertThrows(
                InvalidShipmentStatusTransitionException.class,
                () -> shipment.transitionTo(null)
        );

        assertEquals(ShipmentStatus.CREATED, shipment.getStatus());
    }
}