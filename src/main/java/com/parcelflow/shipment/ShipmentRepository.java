package com.parcelflow.shipment;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {
    Optional<Shipment> findByTrackingNumber(String trakingNumber);

    boolean existsByTrackingNumber(String trakingNumber);

}
