package com.nure.apz.fatianov.daniil.orderservice.request;

import com.nure.apz.fatianov.daniil.orderservice.order.Item;
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
public class OrderAddRequestBody {
    private String arrivalStationNumber;
    private ZonedDateTime creationDate;
    private List<Item> items;
}

