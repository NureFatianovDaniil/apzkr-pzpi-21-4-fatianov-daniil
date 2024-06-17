package com.nure.apz.fatianov.daniil.vehiclestationservice.station.response;

import com.nure.apz.fatianov.daniil.vehiclestationservice.station.Type;
import com.nure.apz.fatianov.daniil.vehiclestationservice.station.response.objects.VehicleEntityForStation;
import com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle.Vehicle;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StationGetAllResponseEntity {
    private Integer id;
    private String number;
    private String description;
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private Type type;
    private List<VehicleEntityForStation> vehicles;
}
