package com.parcelflow.shipment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentRetrievalServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    private ShipmentService shipmentService;

    @BeforeEach
    void setUp() {
        shipmentService = new ShipmentService(shipmentRepository);
    }

    @Test
    void shouldFindShipmentById() {

        UUID id = UUID.randomUUID();
        Shipment shipment = mock(Shipment.class);

        when(shipmentRepository.findById(id))
                .thenReturn(Optional.of(shipment));

        Shipment result = shipmentService.getShipmentById(id);

        assertSame(shipment, result);

        verify(shipmentRepository).findById(id);
    }

    @Test
    void shouldThrowExceptionWhenIdDoesNotExist() {

        UUID id = UUID.randomUUID();

        when(shipmentRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                ShipmentNotFoundException.class,
                () -> shipmentService.getShipmentById(id)
        );
    }

    @Test
    void shouldFindShipmentByTrackingNumber() {

        String trackingNumber = "PF-123";
        Shipment shipment = mock(Shipment.class);

        when(shipmentRepository.findByTrackingNumber(trackingNumber))
                .thenReturn(Optional.of(shipment));

        Shipment result = shipmentService
                .getShipmentByTrackingNumber(trackingNumber);

        assertSame(shipment, result);

        verify(shipmentRepository)
                .findByTrackingNumber(trackingNumber);
    }

    @Test
    void shouldThrowExceptionWhenTrackingNumberDoesNotExist() {

        String trackingNumber = "PF-UNKNOWN";

        when(shipmentRepository.findByTrackingNumber(trackingNumber))
                .thenReturn(Optional.empty());

        assertThrows(
                ShipmentNotFoundException.class,
                () -> shipmentService
                        .getShipmentByTrackingNumber(trackingNumber)
        );
    }

    @Test
    void shouldRetrievePaginatedShipments() {

        Shipment shipment = mock(Shipment.class);

        Page<Shipment> pageResult = new PageImpl<>(
                List.of(shipment),
                PageRequest.of(0, 10),
                1
        );

        when(shipmentRepository.findAll(any(Pageable.class)))
                .thenReturn(pageResult);

        Page<Shipment> result = shipmentService.getShipments(0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(shipmentRepository).findAll(captor.capture());

        Pageable pageable = captor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());

        assertEquals(
                Sort.Direction.DESC,
                pageable.getSort()
                        .getOrderFor("createdAt")
                        .getDirection()
        );

        assertEquals(
                Sort.Direction.DESC,
                pageable.getSort()
                        .getOrderFor("id")
                        .getDirection()
        );
    }

    @Test
    void shouldRejectInvalidPagination() {

        assertThrows(
                IllegalArgumentException.class,
                () -> shipmentService.getShipments(-1, 10)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> shipmentService.getShipments(0, 101)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> shipmentService.getShipments(0, 0)
        );

        verifyNoInteractions(shipmentRepository);
    }
}
