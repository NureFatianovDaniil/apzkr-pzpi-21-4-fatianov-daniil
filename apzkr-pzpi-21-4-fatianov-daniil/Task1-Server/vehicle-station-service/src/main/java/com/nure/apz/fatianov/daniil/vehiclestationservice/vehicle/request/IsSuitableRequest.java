package com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IsSuitableRequest {
    private String vehicleNumber;
    private Double weight;
    private Integer departureStationId;
    private Integer arrivalStationId;
}
