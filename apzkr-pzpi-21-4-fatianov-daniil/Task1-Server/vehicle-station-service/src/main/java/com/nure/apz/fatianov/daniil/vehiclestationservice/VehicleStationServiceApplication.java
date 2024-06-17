package com.nure.apz.fatianov.daniil.vehiclestationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VehicleStationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(VehicleStationServiceApplication.class, args);
	}

}
