package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.security;

import com.bancotiempo.pasaportesaberes.domain.model.Credencial;
import com.bancotiempo.pasaportesaberes.domain.model.Habilidad;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class HmacFirmaCredencialesTest {
    private final HmacFirmaCredenciales firma =
            new HmacFirmaCredenciales("clave-de-firma-de-pruebas-de-32-bytes");

    @Test
    void verificaLaFirmaTrasLaNormalizacionTimestampsDePostgres() {
        Persona persona = persona(11L);
        Habilidad habilidad = new Habilidad(12L, "Habilidad", "General");
        OffsetDateTime fechaFirmada =
                OffsetDateTime.parse("2026-11-24T10:00:00.123456789-05:00");
        String firmaOriginal = firma.firmar(
                persona.getId(),
                habilidad.id(),
                new BigDecimal("4.25"),
                new BigDecimal("4.50"),
                "codigo-verificacion",
                fechaFirmada
        );
        OffsetDateTime fechaLeidaDeBase = OffsetDateTime.ofInstant(
                fechaFirmada.toInstant().truncatedTo(ChronoUnit.MICROS),
                ZoneOffset.UTC
        );
        Credencial persistida = new Credencial(
                13L,
                persona,
                habilidad,
                new BigDecimal("4.25"),
                new BigDecimal("4.50"),
                "codigo-verificacion",
                firmaOriginal,
                fechaLeidaDeBase,
                0
        );

        assertThat(firma.verificar(persistida)).isTrue();
    }

    @Test
    void rechazaCredencialModificadaDespuesDeFirmarse() {
        Persona persona = persona(11L);
        Habilidad habilidad = new Habilidad(12L, "Habilidad", "General");
        OffsetDateTime ahora = OffsetDateTime.parse("2026-11-24T10:00:00Z");
        String firmaOriginal = firma.firmar(
                persona.getId(),
                habilidad.id(),
                new BigDecimal("4.25"),
                new BigDecimal("4.50"),
                "codigo-verificacion",
                ahora
        );
        Credencial alterada = new Credencial(
                13L,
                persona,
                habilidad,
                new BigDecimal("5.25"),
                new BigDecimal("4.50"),
                "codigo-verificacion",
                firmaOriginal,
                ahora,
                0
        );

        assertThat(firma.verificar(alterada)).isFalse();
    }

    private static Persona persona(Long id) {
        Persona persona = new Persona();
        persona.setId(id);
        return persona;
    }
}
