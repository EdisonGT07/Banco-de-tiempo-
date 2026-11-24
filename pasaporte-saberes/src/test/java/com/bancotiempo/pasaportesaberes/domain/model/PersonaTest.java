package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.PersonaInvalidaException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersonaTest {
    private final OffsetDateTime ahora = OffsetDateTime.parse("2026-11-24T10:00:00Z");
    private final LocalDate fechaActual = ahora.toLocalDate();

    @Test
    void fechaNacimientoDebeCumplirEdadMinimaYNoSerFutura() {
        Persona persona = new Persona();

        persona.registrarFechaNacimiento(LocalDate.of(2008, 11, 24), fechaActual);
        persona.validarMayorDeEdad(fechaActual);

        assertThat(persona.getFechaNacimiento()).isEqualTo(LocalDate.of(2008, 11, 24));
        assertThatThrownBy(() -> persona.registrarFechaNacimiento(
                LocalDate.of(2008, 11, 25), fechaActual
        )).isInstanceOf(PersonaInvalidaException.class);
        assertThatThrownBy(() -> persona.registrarFechaNacimiento(
                fechaActual.plusDays(1), fechaActual
        )).isInstanceOf(PersonaInvalidaException.class);
    }

    @Test
    void estratoPuedeOmitirsePeroSiSeDefineDebeEstarEntreUnoYSeis() {
        Persona persona = new Persona();

        persona.setEstrato(null);
        persona.setEstrato((short) 1);
        persona.setEstrato((short) 6);

        assertThat(persona.getEstrato()).isEqualTo((short) 6);
        assertThatThrownBy(() -> persona.setEstrato((short) 0))
                .isInstanceOf(PersonaInvalidaException.class);
        assertThatThrownBy(() -> persona.setEstrato((short) 7))
                .isInstanceOf(PersonaInvalidaException.class);
    }

    @Test
    void rangosSonOpcionalesPeroDebenCorresponderASuEscala() {
        Persona persona = new Persona();
        Rango ensenanza = rango(Rango.EscalaRango.ENSENANZA);
        Rango aprendizaje = rango(Rango.EscalaRango.APRENDIZAJE);

        persona.setRangoEnsenanza(ensenanza);
        persona.setRangoAprendizaje(aprendizaje);
        persona.setRangoEnsenanza(null);
        persona.setRangoAprendizaje(null);

        assertThat(persona.getRangoEnsenanza()).isNull();
        assertThat(persona.getRangoAprendizaje()).isNull();
        assertThatThrownBy(() -> persona.setRangoEnsenanza(aprendizaje))
                .isInstanceOf(PersonaInvalidaException.class);
        assertThatThrownBy(() -> persona.setRangoAprendizaje(ensenanza))
                .isInstanceOf(PersonaInvalidaException.class);
    }

    @Test
    void suspensionYBloqueoTemporalSonIndependientes() {
        Persona persona = new Persona();
        persona.aplicarBloqueoPorInasistencias(
                List.of(ahora),
                ahora,
                1,
                Duration.ofDays(30),
                Duration.ofDays(7)
        );

        assertThat(persona.getEstado()).isEqualTo(Persona.Estado.ACTIVO);
        assertThat(persona.estaBloqueadaEn(ahora)).isTrue();
        assertThat(persona.estaBloqueadaEn(ahora.plusDays(7))).isFalse();

        persona.suspender();
        assertThat(persona.estaBloqueadaEn(ahora.plusDays(8))).isTrue();
        persona.activar();
        assertThat(persona.estaBloqueadaEn(ahora.plusDays(8))).isFalse();
    }

    @Test
    void alcanzaElUmbralDeInasistenciasYBloqueaPorLaDuracionConfigurada() {
        Persona persona = new Persona();
        List<OffsetDateTime> inasistencias = List.of(
                ahora.minusDays(31),
                ahora.minusDays(20),
                ahora.minusDays(10),
                ahora
        );

        boolean bloqueoAplicado = persona.aplicarBloqueoPorInasistencias(
                inasistencias,
                ahora,
                3,
                Duration.ofDays(30),
                Duration.ofDays(7)
        );

        assertThat(bloqueoAplicado).isTrue();
        assertThat(persona.getBloqueadoHasta()).isEqualTo(ahora.plusDays(7));
        assertThat(persona.getEstado()).isEqualTo(Persona.Estado.ACTIVO);
    }

    @Test
    void noBloqueaSiNoSeAlcanzaElUmbralYNoAcortaUnBloqueoVigente() {
        Persona persona = new Persona();

        boolean bloqueoAplicado = persona.aplicarBloqueoPorInasistencias(
                List.of(ahora.minusDays(1), ahora.minusDays(2)),
                ahora,
                3,
                Duration.ofDays(30),
                Duration.ofDays(7)
        );
        assertThat(bloqueoAplicado).isFalse();
        assertThat(persona.getBloqueadoHasta()).isNull();

        persona.aplicarBloqueoPorInasistencias(
                List.of(ahora),
                ahora,
                1,
                Duration.ofDays(30),
                Duration.ofDays(10)
        );
        persona.aplicarBloqueoPorInasistencias(
                List.of(ahora.minusDays(1), ahora.minusDays(2), ahora.minusDays(3)),
                ahora,
                3,
                Duration.ofDays(30),
                Duration.ofDays(7)
        );
        assertThat(persona.getBloqueadoHasta()).isEqualTo(ahora.plusDays(10));
    }

    private Rango rango(Rango.EscalaRango escala) {
        return new Rango(1L, escala, "Rango", (short) 1, null, null, null, null, null);
    }
}
