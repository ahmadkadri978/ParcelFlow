package com.parcelflow.shipment;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shipment_events")
public class ShipmentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "shipment_id",
            nullable = false,
            updatable = false
    )
    private Shipment shipment;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "event_type",
            nullable = false,
            length = 20,
            updatable = false
    )
    private ShipmentEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "previous_status",
            length = 32,
            updatable = false
    )
    private ShipmentStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "new_status",
            nullable = false,
            length = 32,
            updatable = false
    )
    private ShipmentStatus newStatus;

    @Column(
            name = "sequence_number",
            nullable = false,
            updatable = false
    )
    private long sequenceNumber;

    @Column(
            name = "occurred_at",
            nullable = false,
            updatable = false
    )
    private Instant occurredAt;

    protected ShipmentEvent() {
        // Required by JPA
    }

    private ShipmentEvent(
            Shipment shipment,
            ShipmentEventType eventType,
            ShipmentStatus previousStatus,
            ShipmentStatus newStatus,
            long sequenceNumber
    ) {
        this.shipment = shipment;
        this.eventType = eventType;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.sequenceNumber = sequenceNumber;
    }

    public static ShipmentEvent created(Shipment shipment) {

        return new ShipmentEvent(
                shipment,
                ShipmentEventType.CREATED,
                null,
                ShipmentStatus.CREATED,
                0L
        );
    }

    public static ShipmentEvent statusChanged(
            Shipment shipment,
            ShipmentStatus previousStatus,
            ShipmentStatus newStatus,
            long sequenceNumber
    ) {

        return new ShipmentEvent(
                shipment,
                ShipmentEventType.STATUS_CHANGED,
                previousStatus,
                newStatus,
                sequenceNumber
        );
    }

    @PrePersist
    protected void onCreate() {
        this.occurredAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Shipment getShipment() {
        return shipment;
    }

    public ShipmentEventType getEventType() {
        return eventType;
    }

    public ShipmentStatus getPreviousStatus() {
        return previousStatus;
    }

    public ShipmentStatus getNewStatus() {
        return newStatus;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}