package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.OfertaInvalidaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OfertaTest {
    private final OffsetDateTime ahora = OffsetDateTime.parse("2026-11-24T10:00:00Z");
    private final Persona tutor = persona(1L);
    private final Persona receptor = persona(2L);
    private final Habilidad habilidad = new Habilidad(1L, "Habilidad", "General");

    @Test
    void validaDatosYPrecisionDeLaDuracion() {
        Oferta oferta = oferta(
                new BigDecimal("1.5"),
                Oferta.DestinoHoras.BILLETERA_PROPIA,
                null,
                true
        );

        assertThat(oferta.duracionHoras()).isEqualByComparingTo("1.50");
        assertThatThrownBy(() -> oferta(
                BigDecimal.ZERO, Oferta.DestinoHoras.BILLETERA_PROPIA, null, true
        )).isInstanceOf(OfertaInvalidaException.class);
        assertThatThrownBy(() -> oferta(
                new BigDecimal("1.001"),
                Oferta.DestinoHoras.BILLETERA_PROPIA,
                null,
                true
        )).isInstanceOf(OfertaInvalidaException.class);
        assertThatThrownBy(() -> oferta(
                new BigDecimal("100.00"),
                Oferta.DestinoHoras.BILLETERA_PROPIA,
                null,
                true
        )).isInstanceOf(OfertaInvalidaException.class);
    }

    @Test
    void destinoRegaloExigeReceptorDistintoDelTutor() {
        Oferta regalo = oferta(
                BigDecimal.ONE,
                Oferta.DestinoHoras.REGALO,
                receptor,
                true
        );
        assertThat(regalo.receptorRegalo()).isSameAs(receptor);

        assertThatThrownBy(() -> oferta(
                BigDecimal.ONE, Oferta.DestinoHoras.REGALO, null, true
        )).isInstanceOf(OfertaInvalidaException.class);
        assertThatThrownBy(() -> oferta(
                BigDecimal.ONE, Oferta.DestinoHoras.FONDO, receptor, true
        )).isInstanceOf(OfertaInvalidaException.class);
        assertThatThrownBy(() -> oferta(
                BigDecimal.ONE, Oferta.DestinoHoras.REGALO, tutor, true
        )).isInstanceOf(OfertaInvalidaException.class);
    }

    @Test
    void publicarYDesactivarDevuelvenNuevasInstanciasSinMutarLaOriginal() {
        Oferta borrador = oferta(
                BigDecimal.ONE, Oferta.DestinoHoras.BILLETERA_PROPIA, null, false
        );

        Oferta publicada = borrador.publicar();
        Oferta desactivada = publicada.desactivar();

        assertThat(borrador.activa()).isFalse();
        assertThat(publicada.activa()).isTrue();
        assertThat(desactivada.activa()).isFalse();
        assertThat(publicada).isNotSameAs(borrador);
    }

    private Oferta oferta(
            BigDecimal duracion,
            Oferta.DestinoHoras destino,
            Persona receptorRegalo,
            boolean activa
    ) {
        return new Oferta(
                1L,
                tutor,
                habilidad,
                "Descripción de prueba",
                duracion,
                Oferta.Modalidad.VIRTUAL,
                null,
                destino,
                receptorRegalo,
                activa,
                ahora
        );
    }

    private Persona persona(Long id) {
        Persona persona = new Persona();
        persona.setId(id);
        return persona;
    }
}
