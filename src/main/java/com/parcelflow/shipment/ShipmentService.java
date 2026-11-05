package com.parcelflow.shipment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;

    public ShipmentService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Transactional
    public Shipment createShipment(
            String recipientName,
            String origin,
            String destination
    ) {

        // 1. Validate and clean input
        String validName = validateField(
                recipientName, "Recipient name"
        );

        String validOrigin = validateField(
                origin, "Origin"
        );

        String validDestination = validateField(
                destination, "Destination"
        );

        // 2. Generate a tracking number
        String trackingNumber = generateTrackingNumber();

        // 3. Create the shipment
        Shipment shipment = new Shipment(
                trackingNumber,
                validName,
                validOrigin,
                validDestination
        );

        // 4. Save the shipment
        return shipmentRepository.save(shipment);
    }

    private String generateTrackingNumber() {
        return "PF-" + UUID.randomUUID();
    }

    private String validateField(String value, String fieldName) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }

        String cleanedValue = value.trim();

        if (cleanedValue.length() > 120) {
            throw new IllegalArgumentException(
                    fieldName + " must not exceed 120 characters"
            );
        }

        return cleanedValue;
    }
}
