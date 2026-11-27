package com.parcelflow.shipment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShipmentEventRepository
        extends JpaRepository<ShipmentEvent, UUID> {

    List<ShipmentEvent> findByShipment_IdOrderBySequenceNumberAsc(
            UUID shipmentId
    );
}