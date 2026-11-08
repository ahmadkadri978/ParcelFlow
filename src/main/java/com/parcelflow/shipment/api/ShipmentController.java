package com.parcelflow.shipment.api;

import com.parcelflow.shipment.Shipment;
import com.parcelflow.shipment.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

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

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponse> getShipmentById(
            @PathVariable UUID id
    ) {

        Shipment shipment = shipmentService.getShipmentById(id);

        return ResponseEntity.ok(
                ShipmentResponse.from(shipment)
        );
    }

    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<ShipmentResponse> getShipmentByTrackingNumber(
            @PathVariable String trackingNumber
    ) {

        Shipment shipment = shipmentService
                .getShipmentByTrackingNumber(trackingNumber);

        return ResponseEntity.ok(
                ShipmentResponse.from(shipment)
        );
    }

    @GetMapping
    public ResponseEntity<ShipmentPageResponse> getShipments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<Shipment> shipments = shipmentService
                .getShipments(page, size);

        return ResponseEntity.ok(
                ShipmentPageResponse.from(shipments)
        );
    }
}
