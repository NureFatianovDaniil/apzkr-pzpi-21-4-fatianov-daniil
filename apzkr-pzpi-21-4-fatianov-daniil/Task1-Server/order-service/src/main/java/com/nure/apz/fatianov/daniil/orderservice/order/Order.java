package com.nure.apz.fatianov.daniil.orderservice.order;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
@Document
public class Order {
    @Id
    private String id;
    private Integer userId;
    private Integer vehicleId;
    private Integer departureStationId;
    private Integer arrivalStationId;
    private String number;
    private String receiptCode;
    private Instant creationDate;
    private Status status;
    private List<Item> items;

    public Order(Integer userId,
                 Integer vehicleId,
                 Integer departureStationId,
                 Integer arrivalStationId,
                 String number,
                 List<Item> items) {
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.departureStationId = departureStationId;
        this.arrivalStationId = arrivalStationId;
        this.number = number;
        this.items = items;
    }

    public Order(Integer userId,
                 Integer arrivalStationId,
                 String number,
                 List<Item> items) {
        this.userId = userId;
        this.arrivalStationId = arrivalStationId;
        this.number = number;
        this.items = items;
    }
}
