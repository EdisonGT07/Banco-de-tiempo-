package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.EvidenciaInvalidaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvidenciaTest {
    private final OffsetDateTime ahora = OffsetDateTime.parse("2026-11-24T10:00:00Z");
    private final Sesion sesion = new Sesion(
            1L,
            new Oferta(
                    2L,
                    new Persona(),
                    new Habilidad(1L, "Habilidad", "General"),
                    "Oferta",
                    BigDecimal.ONE,
                    Oferta.Modalidad.VIRTUAL,
                    null,
                    Oferta.DestinoHoras.BILLETERA_PROPIA,
                    null,
                    true,
                    ahora
            ),
            new Persona(),
            ahora,
            null,
            Sesion.EstadoSesion.RESERVADA,
            null,
            null,
            null,
            0,
            ahora
    );

    @Test
    void evidenciaSeConsumeUnaSolaVezAntesDeExpirar() {
        Evidencia evidencia = evidencia();

        Evidencia consumida = evidencia.consumir(ahora.plusMinutes(5));

        assertThat(evidencia.estaConsumida()).isFalse();
        assertThat(consumida.estaConsumida()).isTrue();
        assertThat(consumida.usadoEn()).isEqualTo(ahora.plusMinutes(5));
        assertThatThrownBy(() -> consumida.consumir(ahora.plusMinutes(6)))
                .isInstanceOf(EvidenciaInvalidaException.class);
    }

    @Test
    void evidenciaExpiradaNoSePuedeConsumirNiRegistrarIntentos() {
        Evidencia evidencia = evidencia();
        OffsetDateTime expiraEn = evidencia.expiraEn();

        assertThat(evidencia.estaExpiradaEn(expiraEn)).isTrue();
        assertThatThrownBy(() -> evidencia.consumir(expiraEn))
                .isInstanceOf(EvidenciaInvalidaException.class);
        assertThatThrownBy(() -> evidencia.registrarIntentoFallido(expiraEn))
                .isInstanceOf(EvidenciaInvalidaException.class);
    }

    @Test
    void bloqueaAlQuintoIntentoFallido() {
        Evidencia evidencia = evidencia();
        for (int intento = 0; intento < 5; intento++) {
            evidencia = evidencia.registrarIntentoFallido(ahora.plusSeconds(intento + 1));
        }

        assertThat(evidencia.intentosFallidos()).isEqualTo((short) 5);
        assertThat(evidencia.estaBloqueadaPorIntentos()).isTrue();
        Evidencia bloqueada = evidencia;
        assertThatThrownBy(() -> bloqueada.consumir(ahora.plusMinutes(1)))
                .isInstanceOf(EvidenciaInvalidaException.class);
        assertThatThrownBy(() -> bloqueada.registrarIntentoFallido(ahora.plusMinutes(1)))
                .isInstanceOf(EvidenciaInvalidaException.class);
    }

    @Test
    void validaHashesCoordenadasYParejaLatitudLongitud() {
        Evidencia evidencia = new Evidencia(
                1L,
                sesion,
                hash("a"),
                hash("b"),
                ahora.plusMinutes(15),
                null,
                (short) 0,
                null,
                new BigDecimal("90.000000"),
                new BigDecimal("-180.000000"),
                ahora
        );
        assertThat(evidencia.latitud()).isEqualByComparingTo("90.000000");
        assertThat(evidencia.longitud()).isEqualByComparingTo("-180.000000");

        assertThatThrownBy(() -> new Evidencia(
                2L, sesion, "not-a-hash", hash("b"), ahora.plusMinutes(15),
                null, (short) 0, null, null, null, ahora
        )).isInstanceOf(EvidenciaInvalidaException.class);
        assertThatThrownBy(() -> new Evidencia(
                3L, sesion, hash("a"), hash("b"), ahora.plusMinutes(15),
                null, (short) 0, null, new BigDecimal("1"), null, ahora
        )).isInstanceOf(EvidenciaInvalidaException.class);
        assertThatThrownBy(() -> new Evidencia(
                4L, sesion, hash("a"), hash("b"), ahora.plusMinutes(15),
                null, (short) 0, null, new BigDecimal("90.000001"), BigDecimal.ZERO, ahora
        )).isInstanceOf(EvidenciaInvalidaException.class);
    }

    private Evidencia evidencia() {
        return new Evidencia(
                1L,
                sesion,
                hash("a"),
                hash("b"),
                ahora.plusMinutes(15),
                null,
                (short) 0,
                null,
                null,
                null,
                ahora
        );
    }

    private String hash(String caracter) {
        return caracter.repeat(64);
    }
}
