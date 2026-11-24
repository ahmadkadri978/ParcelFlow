package com.parcelflow.shipment;

//Later in REST API, I will translate this exception into HTTP 409 Conflict
public class InvalidShipmentStatusTransitionException extends RuntimeException{
    public InvalidShipmentStatusTransitionException(ShipmentStatus currentStatus,
                                                    ShipmentStatus requestedStatus){
        super(
                "Cannot change shipment status from "
                        + currentStatus
                        + " to "
                        + requestedStatus
        );
    }
}
