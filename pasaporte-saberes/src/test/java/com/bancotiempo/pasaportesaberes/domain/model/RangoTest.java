package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.RangoInvalidoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RangoTest {
    @Test
    void seleccionaElRangoMasAltoQueCumpleTodosLosUmbralesDeEnsenanza() {
        List<Rango> catalogo = List.of(
                rango(Rango.EscalaRango.ENSENANZA, "Pedagogo", 1, "1", null, null, null, null),
                rango(Rango.EscalaRango.ENSENANZA, "Mentor", 2, "10", "4.0", 5, null, null),
                rango(Rango.EscalaRango.ENSENANZA, "Maestro", 3, "50", "4.2", 15, null, null),
                rango(Rango.EscalaRango.ENSENANZA, "Filosofo", 4, "150", "4.4", 40, null, null)
        );

        Optional<Rango> seleccionado = Rango.seleccionarRango(
                catalogo,
                Rango.EscalaRango.ENSENANZA,
                new BigDecimal("50"),
                new BigDecimal("4.2"),
                15,
                0,
                BigDecimal.ZERO
        );

        assertThat(seleccionado).isPresent();
        assertThat(seleccionado.orElseThrow().nombre()).isEqualTo("Maestro");
    }

    @Test
    void respetaAprendicesHabilidadesYAsistenciaEnLaEscalaDeAprendizaje() {
        List<Rango> catalogo = List.of(
                rango(Rango.EscalaRango.APRENDIZAJE, "Oyente", 1, "1", null, null, null, null),
                rango(Rango.EscalaRango.APRENDIZAJE, "Discipulo", 2, "10", null, null, 2, null),
                rango(Rango.EscalaRango.APRENDIZAJE, "Estudioso", 3, "40", null, null, 4, "0.900")
        );

        Optional<Rango> bajoAsistencia = Rango.seleccionarRango(
                catalogo,
                Rango.EscalaRango.APRENDIZAJE,
                new BigDecimal("40"),
                null,
                null,
                4,
                new BigDecimal("0.899")
        );
        Optional<Rango> suficienteAsistencia = Rango.seleccionarRango(
                catalogo,
                Rango.EscalaRango.APRENDIZAJE,
                new BigDecimal("40"),
                null,
                null,
                4,
                new BigDecimal("0.900")
        );

        assertThat(bajoAsistencia.orElseThrow().nombre()).isEqualTo("Discipulo");
        assertThat(suficienteAsistencia.orElseThrow().nombre()).isEqualTo("Estudioso");
    }

    @Test
    void noDevuelveRangoCuandoNingunoCumpleYRechazaCatalogosInvalidos() {
        List<Rango> catalogo = List.of(
                rango(Rango.EscalaRango.ENSENANZA, "Pedagogo", 1, "1", null, null, null, null)
        );

        assertThat(Rango.seleccionarRango(
                catalogo,
                Rango.EscalaRango.ENSENANZA,
                BigDecimal.ZERO,
                null,
                null,
                null,
                null
        )).isEmpty();
        assertThatThrownBy(() -> Rango.seleccionarRango(
                Collections.singletonList(null),
                Rango.EscalaRango.ENSENANZA,
                BigDecimal.ONE,
                null,
                null,
                null,
                null
        )).isInstanceOf(RangoInvalidoException.class);
        assertThatThrownBy(() -> rango(
                Rango.EscalaRango.ENSENANZA, "Invalido", 1, "1", "5.01", null, null, null
        )).isInstanceOf(RangoInvalidoException.class);
    }

    private Rango rango(
            Rango.EscalaRango escala,
            String nombre,
            int orden,
            String minimoHoras,
            String minimoRating,
            Integer minimoAprendices,
            Integer minimoHabilidades,
            String minimoAsistencia
    ) {
        return new Rango(
                1L,
                escala,
                nombre,
                (short) orden,
                new BigDecimal(minimoHoras),
                minimoRating == null ? null : new BigDecimal(minimoRating),
                minimoAprendices,
                minimoHabilidades,
                minimoAsistencia == null ? null : new BigDecimal(minimoAsistencia)
        );
    }
}
