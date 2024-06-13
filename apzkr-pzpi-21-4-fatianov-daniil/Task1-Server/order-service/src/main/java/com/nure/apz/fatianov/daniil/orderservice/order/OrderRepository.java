package com.nure.apz.fatianov.daniil.orderservice.order;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends MongoRepository<Order, String> {
    Optional<Order> findByNumberAndStatusNotLike (String number, Status status);

    Optional<Order> findByVehicleIdAndStatusLike(Integer vehicleId, Status status);

    Optional<List<Order>> findAllByUserId(Integer userId);
}
