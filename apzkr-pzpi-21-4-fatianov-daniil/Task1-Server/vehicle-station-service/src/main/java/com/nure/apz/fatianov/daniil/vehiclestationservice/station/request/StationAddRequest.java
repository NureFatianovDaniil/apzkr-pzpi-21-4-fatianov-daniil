package com.nure.apz.fatianov.daniil.vehiclestationservice.station.request;

import com.nure.apz.fatianov.daniil.vehiclestationservice.station.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StationAddRequest {
    private String description;
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private Type type;
}
