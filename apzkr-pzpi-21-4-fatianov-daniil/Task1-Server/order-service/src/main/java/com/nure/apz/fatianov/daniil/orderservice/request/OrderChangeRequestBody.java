package com.nure.apz.fatianov.daniil.orderservice.request;

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
public class OrderChangeRequestBody {
    private String id;
//    private Integer userId;
//    private Integer vehicleId;
//    private Integer departureStationId;
    private String arrivalStationNumber;
    private List<Item> items;
}
