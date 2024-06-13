package com.nure.apz.fatianov.daniil.orderservice.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IsSuitableRequestEntity {
    private Double weight;
    private Integer departureStationId;
    private Integer arrivalStationId;
}
