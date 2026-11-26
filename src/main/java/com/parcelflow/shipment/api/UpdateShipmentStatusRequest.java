package com.parcelflow.shipment.api;

import com.parcelflow.shipment.ShipmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateShipmentStatusRequest(

        @NotNull(message = "Status is required")
        ShipmentStatus status

) {
}