package com.nure.apz.fatianov.daniil.orderservice.order;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Item {
    private String name;
    private String description;
    private Integer quantity;
    private Float weight;
    private Boolean isFragile;

}
