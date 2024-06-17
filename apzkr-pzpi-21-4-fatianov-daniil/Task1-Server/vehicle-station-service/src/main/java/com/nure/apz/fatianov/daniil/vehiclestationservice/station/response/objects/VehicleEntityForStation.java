package com.nure.apz.fatianov.daniil.vehiclestationservice.station.response.objects;

import com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleEntityForStation {
    private String number;
    private Status status;
}
