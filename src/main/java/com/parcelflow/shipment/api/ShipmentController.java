package com.parcelflow.shipment.api;

import com.parcelflow.shipment.Shipment;
import com.parcelflow.shipment.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {
    private final ShipmentService shipmentService;
    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }
    @PostMapping
    public ResponseEntity<ShipmentResponse> createShipment(
            @Valid @RequestBody CreateShipmentRequest request
    ) {
        Shipment shipment= shipmentService.createShipment(
                request.recipientName(),
                request.origin(),
                request.destination()
        );
        ShipmentResponse response = ShipmentResponse.from(shipment);
        URI location = URI.create("/api/v1/shipments/" + shipment.getId());
        return ResponseEntity.created(location).body(response);
    }
}
