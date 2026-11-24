package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.HabilidadInvalidaException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HabilidadTest {
    @Test
    void requiereNombreYCategoriaDentroDeLosLimitesDelEsquema() {
        new Habilidad(null, "Guitarra", "Música");

        assertThatThrownBy(() -> new Habilidad(null, " ", "Música"))
                .isInstanceOf(HabilidadInvalidaException.class);
        assertThatThrownBy(() -> new Habilidad(null, "Guitarra", " "))
                .isInstanceOf(HabilidadInvalidaException.class);
        assertThatThrownBy(() -> new Habilidad(0L, "Guitarra", "Música"))
                .isInstanceOf(HabilidadInvalidaException.class);
        assertThatThrownBy(() -> new Habilidad(null, "x".repeat(81), "Música"))
                .isInstanceOf(HabilidadInvalidaException.class);
    }
}
