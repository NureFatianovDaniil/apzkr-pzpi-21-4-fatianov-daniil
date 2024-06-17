package com.nure.apz.fatianov.daniil.vehiclestationservice.station;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface StationRepository extends JpaRepository<Station, Integer> {
    Optional<Station> findByNumber(String stationNumber);

    @Query(value = "SELECT nextval('station_seq')", nativeQuery = true)
    Integer getNextSequenceValue();
}
