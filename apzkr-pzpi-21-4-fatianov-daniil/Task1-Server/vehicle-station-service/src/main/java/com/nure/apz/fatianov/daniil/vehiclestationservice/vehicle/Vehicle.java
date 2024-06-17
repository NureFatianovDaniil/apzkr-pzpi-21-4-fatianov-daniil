package com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle;

import com.nure.apz.fatianov.daniil.vehiclestationservice.station.Station;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vehicle")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vehicle_seq_gen")
    @SequenceGenerator(name = "vehicle_seq_gen", sequenceName = "vehicle_seq", allocationSize = 1)    private Integer id;
    private String number;
    private Double liftingCapacity;// in kilograms
    private Double flightDistance;// in kilometers
    @ManyToOne
    @JoinColumn(name = "station_id")
    private Station station;
    @Enumerated(EnumType.STRING)
    private Status status;

}
