package com.nure.apz.fatianov.daniil.orderservice.response;

import com.nure.apz.fatianov.daniil.orderservice.order.Item;
import com.nure.apz.fatianov.daniil.orderservice.order.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderUserGetResponse {
    private String id;
    private String arrivalStationNumber;
    private String departureStationNumber;
    private String number;
    private String receiptCode;
    private ZonedDateTime creationDate;
    private Status status;
    private List<Item> items;
}
