package com.parcelflow.shipment;

public enum ShipmentStatus {

    CREATED,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED;

    /* Can a shipment move from its current status to the requested status?

          ShipmentStatus.CREATED.canTransitionTo(
            ShipmentStatus.PICKED_UP
            );

            Returns:
            true
     */
    public boolean canTransitionTo(ShipmentStatus nextStatus) {

        return switch (this) {
            case CREATED ->
                    nextStatus == PICKED_UP;

            case PICKED_UP ->
                    nextStatus == IN_TRANSIT;

            case IN_TRANSIT ->
                    nextStatus == OUT_FOR_DELIVERY;

            case OUT_FOR_DELIVERY ->
                    nextStatus == DELIVERED;

            case DELIVERED ->
                    false;
        };
    }
}
