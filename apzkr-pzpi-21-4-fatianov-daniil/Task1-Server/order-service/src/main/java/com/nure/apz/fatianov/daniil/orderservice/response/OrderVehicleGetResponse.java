package com.nure.apz.fatianov.daniil.orderservice.response;

import com.nure.apz.fatianov.daniil.orderservice.order.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderVehicleGetResponse {
    private String arrivalStationNumber;
    private String number;
    private List<Item> items;
}
