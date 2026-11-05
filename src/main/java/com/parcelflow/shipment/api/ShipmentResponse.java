package com.parcelflow.shipment.api;

import com.parcelflow.shipment.Shipment;
import com.parcelflow.shipment.ShipmentStatus;

import java.time.Instant;
import java.util.UUID;

public record ShipmentResponse(

        UUID id,
        String trackingNumber,
        String recipientName,
        String origin,
        String destination,
        ShipmentStatus status,
        Instant createdAt

) {

    public static ShipmentResponse from(Shipment shipment) {

        return new ShipmentResponse(
                shipment.getId(),
                shipment.getTrackingNumber(),
                shipment.getRecipientName(),
                shipment.getOrigin(),
                shipment.getDestination(),
                shipment.getStatus(),
                shipment.getCreatedAt()
        );
    }
}
