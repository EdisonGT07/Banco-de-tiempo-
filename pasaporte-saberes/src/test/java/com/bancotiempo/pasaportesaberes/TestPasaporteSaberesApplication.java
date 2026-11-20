package com.bancotiempo.pasaportesaberes;

import org.springframework.boot.SpringApplication;

public class TestPasaporteSaberesApplication {

	public static void main(String[] args) {
		SpringApplication.from(PasaporteSaberesApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
