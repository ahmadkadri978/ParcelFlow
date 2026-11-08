package com.parcelflow.shipment.api;

import com.parcelflow.shipment.Shipment;

import org.springframework.data.domain.Page;

import java.util.List;

public record ShipmentPageResponse(

        List<ShipmentResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext

) {

    public static ShipmentPageResponse from(
            Page<Shipment> shipments
    ) {

        List<ShipmentResponse> content = shipments
                .getContent()
                .stream()
                .map(ShipmentResponse::from)
                .toList();

        return new ShipmentPageResponse(
                content,
                shipments.getNumber(),
                shipments.getSize(),
                shipments.getTotalElements(),
                shipments.getTotalPages(),
                shipments.hasNext()
        );
    }
}
