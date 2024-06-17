package com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
    Optional<Vehicle> findByNumber(String number);

    @Query(value = "SELECT nextval('vehicle_seq')", nativeQuery = true)
    Integer getNextSequenceValue();
}
