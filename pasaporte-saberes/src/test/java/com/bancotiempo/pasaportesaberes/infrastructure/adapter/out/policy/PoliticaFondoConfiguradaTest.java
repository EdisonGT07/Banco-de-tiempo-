package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.policy;

import com.bancotiempo.pasaportesaberes.application.port.out.Reloj;
import com.bancotiempo.pasaportesaberes.domain.model.Billetera;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PoliticaFondoConfiguradaTest {
    @Test
    void permiteRetiroSoloCuandoLaPersonaActivaNoTieneSaldoDisponible() {
        Reloj reloj = mock(Reloj.class);
        when(reloj.ahora()).thenReturn(OffsetDateTime.parse("2026-01-01T12:00:00Z"));
        PoliticaFondoConfigurada politica =
                new PoliticaFondoConfigurada(new BigDecimal("5.00"), reloj);
        Persona persona = new Persona();
        persona.setId(7L);
        Billetera billetera = billetera(persona, "0.00");

        assertTrue(politica.puedeRetirar(persona, billetera));
        assertFalse(politica.puedeRetirar(persona, billetera(persona, "0.01")));
        persona.suspender();
        assertFalse(politica.puedeRetirar(persona, billetera));
    }

    private static Billetera billetera(Persona persona, String saldo) {
        return new Billetera(
                11L,
                Billetera.TipoBilletera.USUARIO,
                persona,
                new BigDecimal(saldo),
                BigDecimal.ZERO,
                0
        );
    }
}
