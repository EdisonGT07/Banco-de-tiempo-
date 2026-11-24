package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.AvalInvalidoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvalTest {
    private final LocalDate fecha = LocalDate.of(2026, 11, 24);
    private final Credencial credencial = new Credencial(
            1L,
            new Persona(),
            new Habilidad(1L, "Habilidad", "General"),
            new BigDecimal("1.00"),
            null,
            "codigo",
            "firma",
            OffsetDateTime.parse("2026-11-24T10:00:00Z"),
            0
    );
    private final EntidadAliada entidad = new EntidadAliada();

    @Test
    void avalEsVigenteDentroDeSusFechasInclusive() {
        Aval aval = new Aval(
                1L,
                entidad,
                credencial,
                fecha,
                fecha.plusDays(30),
                Aval.EstadoAval.VIGENTE
        );

        assertThat(aval.estaVigenteEn(fecha)).isTrue();
        assertThat(aval.estaVigenteEn(fecha.plusDays(30))).isTrue();
        assertThat(aval.estaVigenteEn(fecha.minusDays(1))).isFalse();
        assertThat(aval.estaVigenteEn(fecha.plusDays(31))).isFalse();
    }

    @Test
    void revocarEsInmutableYNoSePuedeUsarComoAvalVigente() {
        Aval vigente = new Aval(
                1L,
                entidad,
                credencial,
                fecha,
                fecha.plusDays(30),
                Aval.EstadoAval.VIGENTE
        );
        Aval revocado = vigente.revocar();

        assertThat(vigente.estado()).isEqualTo(Aval.EstadoAval.VIGENTE);
        assertThat(revocado.estado()).isEqualTo(Aval.EstadoAval.REVOCADO);
        assertThat(revocado.estaVigenteEn(fecha.plusDays(1))).isFalse();
        assertThat(revocado.revocar()).isSameAs(revocado);
    }

    @Test
    void vigenciaNoPuedeTerminarAntesDeLaEmision() {
        assertThatThrownBy(() -> new Aval(
                2L,
                entidad,
                credencial,
                fecha,
                fecha.minusDays(1),
                Aval.EstadoAval.VIGENTE
        )).isInstanceOf(AvalInvalidoException.class);
    }
}
