package com.bancotiempo.pasaportesaberes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence")
@EnableJpaRepositories(basePackages = "com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence")
public class PasaporteSaberesApplication {

	public static void main(String[] args) {
		SpringApplication.run(PasaporteSaberesApplication.class, args);
	}

}
