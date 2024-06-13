package com.nure.apz.fatianov.daniil.orderservice.response;

import com.nure.apz.fatianov.daniil.orderservice.order.Item;
import com.nure.apz.fatianov.daniil.orderservice.order.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderAdminGetResponseEntity {
    private String id;
    private String userEmail;
    private String vehicleNumber;
    private String departureStationNumber;
    private String arrivalStationNumber;
    private String number;
    private String receiptCode;
    private ZonedDateTime creationDate;
    private Status status;
    private List<Item> items;
}
