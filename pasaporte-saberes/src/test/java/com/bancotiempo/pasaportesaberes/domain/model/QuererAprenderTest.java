package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.QuererAprenderInvalidoException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuererAprenderTest {
    private final Persona persona = persona(3L);
    private final Habilidad habilidad = new Habilidad(5L, "Inglés", "Idiomas");
    private final OffsetDateTime creadaEn = OffsetDateTime.parse("2026-11-24T10:00:00Z");

    @Test
    void crearComponeLaClaveConLaPersonaYHabilidad() {
        QuererAprender solicitud = QuererAprender.crear(persona, habilidad, creadaEn);

        assertThat(solicitud.id()).isEqualTo(new QuererAprenderId(3L, 5L));
        assertThat(solicitud.persona()).isSameAs(persona);
        assertThat(solicitud.habilidad()).isSameAs(habilidad);
    }

    @Test
    void rechazaIdentificadoresQueNoCorrespondanConLaRelacion() {
        assertThatThrownBy(() -> new QuererAprender(
                new QuererAprenderId(4L, 5L), persona, habilidad, creadaEn
        )).isInstanceOf(QuererAprenderInvalidoException.class);
        assertThatThrownBy(() -> new QuererAprenderId(null, 5L))
                .isInstanceOf(QuererAprenderInvalidoException.class);
        assertThatThrownBy(() -> QuererAprender.crear(persona, habilidad, null))
                .isInstanceOf(QuererAprenderInvalidoException.class);
    }

    private Persona persona(Long id) {
        Persona persona = new Persona();
        persona.setId(id);
        return persona;
    }
}
