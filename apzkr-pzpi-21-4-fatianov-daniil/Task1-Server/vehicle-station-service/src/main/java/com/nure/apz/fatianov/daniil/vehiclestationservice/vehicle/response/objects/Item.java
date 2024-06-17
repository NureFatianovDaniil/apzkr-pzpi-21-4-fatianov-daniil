package com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle.response.objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Item {
    private String name;
    private String description;
    private Integer quantity;
    private Float weight;
    private Boolean isFragile;
}
