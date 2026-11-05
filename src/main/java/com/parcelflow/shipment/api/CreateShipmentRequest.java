package com.parcelflow.shipment.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateShipmentRequest(

        @NotBlank(message = "Recipient name is required")
        @Size(max = 120)
        String recipientName,

        @NotBlank(message = "Origin is required")
        @Size(max = 120)
        String origin,

        @NotBlank(message = "Destination is required")
        @Size(max = 120)
        String destination

) {
}