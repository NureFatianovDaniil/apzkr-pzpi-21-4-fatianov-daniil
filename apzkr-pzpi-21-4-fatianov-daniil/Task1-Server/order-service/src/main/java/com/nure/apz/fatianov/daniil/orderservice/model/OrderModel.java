package com.nure.apz.fatianov.daniil.orderservice.model;

import com.nure.apz.fatianov.daniil.orderservice.order.Item;
import com.nure.apz.fatianov.daniil.orderservice.order.Order;
import com.nure.apz.fatianov.daniil.orderservice.order.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderModel {
    private String id;
    private Integer userId;
    private Integer vehicleId;
    private Integer departureStationId;
    private Integer arrivalStationId;
    private String number;
    private String receiptCode;
    private ZonedDateTime creationDate;
    private Status status;
    private List<Item> items;

    public static OrderModel toModel(Order order) {
        OrderModel model = new OrderModel();
        model.setId(order.getId());
        model.setUserId(order.getUserId());
        model.setVehicleId(order.getVehicleId());
        model.setDepartureStationId(order.getDepartureStationId());
        model.setArrivalStationId(order.getArrivalStationId());
        model.setNumber(order.getNumber());
        model.setReceiptCode(order.getReceiptCode());
        model.setCreationDate(order.getCreationDate().atZone(ZoneOffset.UTC));
        model.setStatus(order.getStatus());
        model.setItems(order.getItems());
        return model;
    }
}
