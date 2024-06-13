package com.nure.apz.fatianov.daniil.orderservice.request;

import com.nure.apz.fatianov.daniil.orderservice.order.Item;
import com.nure.apz.fatianov.daniil.orderservice.order.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderSendRequestBody {
    @Id
    private String id;
    private Integer userId;
    private Integer vehicleId;
    private Integer departureStationId;
    private Integer arrivalStationId;
}
