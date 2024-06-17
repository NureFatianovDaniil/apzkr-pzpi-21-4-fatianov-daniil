package com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle.response.objects;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Order {
    private String arrivalStationNumber;
    private String number;
    private List<Item> items;

    public static boolean orderHasDetails(Order order) {
        return order.getArrivalStationNumber() != null ||
                order.getNumber() != null ||
                order.getItems() != null;

    }
}
