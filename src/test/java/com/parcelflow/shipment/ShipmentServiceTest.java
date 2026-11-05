package com.parcelflow.shipment;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    private ShipmentService shipmentService;

    @BeforeEach
    void setUp() {
        shipmentService = new ShipmentService(shipmentRepository);
    }

    @Test
    void shouldCreateShipmentSuccessfully() {

        when(shipmentRepository.save(any(Shipment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Shipment result = shipmentService.createShipment(
                "John Smith",
                "Berlin",
                "Hamburg"
        );

        assertNotNull(result);

        assertEquals("John Smith", result.getRecipientName());
        assertEquals("Berlin", result.getOrigin());
        assertEquals("Hamburg", result.getDestination());

        assertEquals(
                ShipmentStatus.CREATED,
                result.getStatus()
        );

        assertTrue(result.getTrackingNumber().startsWith("PF-"));

        assertEquals(39, result.getTrackingNumber().length());

        UUID.fromString(result.getTrackingNumber().substring(3));

        verify(shipmentRepository).save(any(Shipment.class));
    }

    @Test
    void shouldTrimInputFields() {

        when(shipmentRepository.save(any(Shipment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Shipment result = shipmentService.createShipment(
                "  John Smith  ",
                "  Berlin  ",
                "  Hamburg  "
        );

        assertEquals("John Smith", result.getRecipientName());
        assertEquals("Berlin", result.getOrigin());
        assertEquals("Hamburg", result.getDestination());
    }

    @Test
    void shouldRejectBlankRecipientName() {

        assertThrows(
                IllegalArgumentException.class,
                () -> shipmentService.createShipment(
                        "   ",
                        "Berlin",
                        "Hamburg"
                )
        );

        verifyNoInteractions(shipmentRepository);
    }

    @Test
    void shouldRejectNullOrigin() {

        assertThrows(
                IllegalArgumentException.class,
                () -> shipmentService.createShipment(
                        "John Smith",
                        null,
                        "Hamburg"
                )
        );

        verifyNoInteractions(shipmentRepository);
    }

    @Test
    void shouldRejectTooLongDestination() {

        String longDestination = "A".repeat(121);

        assertThrows(
                IllegalArgumentException.class,
                () -> shipmentService.createShipment(
                        "John Smith",
                        "Berlin",
                        longDestination
                )
        );

        verifyNoInteractions(shipmentRepository);
    }
}
