package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.policy;

import com.bancotiempo.pasaportesaberes.application.port.out.PoliticaTope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PoliticaTopeConfigurada implements PoliticaTope {
    private final BigDecimal maximoSaldoDisponible;

    public PoliticaTopeConfigurada(
            @Value("${pasaporte-saberes.horas.tope-saldo}") BigDecimal maximoSaldoDisponible
    ) {
        if (maximoSaldoDisponible == null || maximoSaldoDisponible.signum() <= 0) {
            throw new IllegalArgumentException("El tope de saldo debe ser positivo");
        }
        this.maximoSaldoDisponible = maximoSaldoDisponible;
    }

    @Override
    public BigDecimal maximoSaldoDisponible() {
        return maximoSaldoDisponible;
    }
}
