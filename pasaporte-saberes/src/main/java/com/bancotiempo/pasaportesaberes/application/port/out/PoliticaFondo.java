package com.bancotiempo.pasaportesaberes.application.port.out;

import com.bancotiempo.pasaportesaberes.domain.model.Billetera;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;

import java.math.BigDecimal;

public interface PoliticaFondo {
    BigDecimal maximoRetiroPorMes();

    boolean puedeRetirar(Persona persona, Billetera billetera);
}
