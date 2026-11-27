package com.parcelflow.shipment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentEventRepository shipmentEventRepository;

    public ShipmentService(ShipmentRepository shipmentRepository, ShipmentEventRepository shipmentEventRepository) {
        this.shipmentRepository = shipmentRepository;
        this.shipmentEventRepository = shipmentEventRepository;
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
        Shipment savedShipment = shipmentRepository.save(shipment);

        shipmentEventRepository.save(
                ShipmentEvent.created(savedShipment)
        );
        return savedShipment;
    }
    @Transactional(readOnly = true)
    public Shipment getShipmentById(UUID id) {
        return  shipmentRepository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment with id: " + id + " not found"));
    }
    @Transactional(readOnly = true)
    public Shipment getShipmentByTrackingNumber(String trackingNumber) {

        if (trackingNumber == null || trackingNumber.isBlank()) {
            throw new IllegalArgumentException("tracking Number is required");
        }
        String cleanedNumber = trackingNumber.trim();

        return shipmentRepository
                .findByTrackingNumber(cleanedNumber)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment with id: " + cleanedNumber + " not found"));
    }
    @Transactional(readOnly = true)
    public Page<Shipment> getShipments(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be zero or greater");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Size must be between 1 and 100");
        }
        Sort sort = Sort.by(
                Sort.Order.desc("createdAt"),
                Sort.Order.desc("id")
        );
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return shipmentRepository.findAll(pageRequest);

    }
    @Transactional
    public Shipment updateShipmentStatus(
            UUID shipmentId,
            ShipmentStatus nextStatus
    ) {

        Shipment shipment = shipmentRepository
                .findById(shipmentId)
                .orElseThrow(() ->
                        new ShipmentNotFoundException(
                                "Shipment with id: "
                                        + shipmentId
                                        + " not found"
                        )
                );
        ShipmentStatus previousStatus = shipment.getStatus();

        shipment.transitionTo(nextStatus);
        // The next accepted transition uses the next version
        long nextSequence = shipment.getVersion() + 1L;

        // Execute the UPDATE and optimistic-lock version check
        shipmentRepository.flush();
        // Record the historical transition
        ShipmentEvent event = ShipmentEvent.statusChanged(
                shipment,
                previousStatus,
                nextStatus,
                nextSequence
        );

        shipmentEventRepository.save(event);

        return shipment;
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
