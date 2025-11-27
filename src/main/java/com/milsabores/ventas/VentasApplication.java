package com.milsabores.ventas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class VentasApplication {

	public static void main(String[] args) {
		SpringApplication.run(VentasApplication.class, args);
	}

}
