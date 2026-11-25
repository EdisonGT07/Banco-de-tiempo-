package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.policy;

import com.bancotiempo.pasaportesaberes.application.port.out.PoliticaFondo;
import com.bancotiempo.pasaportesaberes.application.port.out.Reloj;
import com.bancotiempo.pasaportesaberes.domain.model.Billetera;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component
public class PoliticaFondoConfigurada implements PoliticaFondo {
    private final BigDecimal maximoRetiroPorMes;
    private final Reloj reloj;

    public PoliticaFondoConfigurada(
            @Value("${pasaporte-saberes.fondo.maximo-retiro-mensual}")
            BigDecimal maximoRetiroPorMes,
            Reloj reloj
    ) {
        if (maximoRetiroPorMes == null || maximoRetiroPorMes.signum() <= 0) {
            throw new IllegalArgumentException(
                    "El máximo mensual de retiro del fondo debe ser positivo");
        }
        this.maximoRetiroPorMes = maximoRetiroPorMes;
        this.reloj = reloj;
    }

    @Override
    public BigDecimal maximoRetiroPorMes() {
        return maximoRetiroPorMes;
    }

    @Override
    public boolean puedeRetirar(Persona persona, Billetera billetera) {
        if (persona == null || billetera == null || persona.getId() == null
                || billetera.getTipo() != Billetera.TipoBilletera.USUARIO
                || billetera.getPersona() == null
                || !Objects.equals(persona.getId(), billetera.getPersona().getId())) {
            return false;
        }
        return !persona.estaBloqueadaEn(reloj.ahora())
                && billetera.getSaldoDisponible().compareTo(BigDecimal.ZERO) == 0;
    }
}
