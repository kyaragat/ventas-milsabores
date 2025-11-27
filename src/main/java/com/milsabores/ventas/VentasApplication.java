package com.milsabores.ventas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VentasApplication {

	public static void main(String[] args) {
		// Configurar las propiedades del sistema para Oracle Wallet
		String walletPath = System.getProperty("user.dir") + "/src/main/resources/Wallet";
		System.setProperty("oracle.net.tns_admin", walletPath);
		System.setProperty("oracle.net.wallet_location", walletPath);
		
		SpringApplication.run(VentasApplication.class, args);
	}

}
