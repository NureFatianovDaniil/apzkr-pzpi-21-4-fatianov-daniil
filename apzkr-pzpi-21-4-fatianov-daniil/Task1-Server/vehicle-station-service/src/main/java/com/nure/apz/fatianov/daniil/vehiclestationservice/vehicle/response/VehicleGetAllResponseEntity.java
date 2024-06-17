package com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle.response;

import com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle.Status;
import com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle.response.objects.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VehicleGetAllResponseEntity {
    private Integer id;
    private String number;
    private Double liftingCapacity;// in kilograms
    private Double flightDistance;// in kilometers
    private String stationNumber;
    private Order order;
    private Status status;
}
