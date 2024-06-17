package com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleAddRequest {
    private Double liftingCapacity;// in kilograms
    private Double flightDistance;// in kilometers
    private String stationNumber;
}
