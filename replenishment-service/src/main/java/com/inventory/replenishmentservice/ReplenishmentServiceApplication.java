package com.inventory.replenishmentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ReplenishmentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReplenishmentServiceApplication.class, args);
	}
}
