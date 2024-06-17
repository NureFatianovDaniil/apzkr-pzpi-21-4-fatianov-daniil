package com.nure.apz.fatianov.daniil.vehiclestationservice.station;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nure.apz.fatianov.daniil.vehiclestationservice.vehicle.Vehicle;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "station")
@ToString(exclude = "vehicles")
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "station_seq_gen")
    @SequenceGenerator(name = "station_seq_gen", sequenceName = "station_seq", allocationSize = 1)
    private Integer id;
    private String number;
    private String description;
    @Enumerated(EnumType.STRING)
    private Type type;
    private Double latitude;
    private Double longitude;
    private Double altitude;
    @OneToMany(mappedBy = "station")
    @JsonIgnore
    private List<Vehicle> vehicles;
}
