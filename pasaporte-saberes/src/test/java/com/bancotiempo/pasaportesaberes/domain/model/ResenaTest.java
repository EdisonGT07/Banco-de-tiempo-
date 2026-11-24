package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.ResenaInvalidaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResenaTest {
    private final OffsetDateTime ahora = OffsetDateTime.parse("2026-11-24T10:00:00Z");
    private final Persona tutor = persona(1L);
    private final Persona aprendiz = persona(2L);
    private final Sesion completada = sesion(Sesion.EstadoSesion.COMPLETADA);

    @Test
    void soloPermiteCalificarUnaSesionCompletadaConNotaDeUnoACinco() {
        Resena resena = resena(completada, Resena.AutorRol.TUTOR, (short) 5);

        assertThat(resena.autor()).isSameAs(tutor);
        assertThat(resena.calificacion()).isEqualTo((short) 5);
        assertThatThrownBy(() -> resena(
                sesion(Sesion.EstadoSesion.EN_CURSO), Resena.AutorRol.TUTOR, (short) 4
        )).isInstanceOf(ResenaInvalidaException.class);
        assertThatThrownBy(() -> resena(
                completada, Resena.AutorRol.APRENDIZ, (short) 0
        )).isInstanceOf(ResenaInvalidaException.class);
        assertThatThrownBy(() -> resena(
                completada, Resena.AutorRol.APRENDIZ, (short) 6
        )).isInstanceOf(ResenaInvalidaException.class);
    }

    @Test
    void crearDeterminaElRolYRechazaPersonasAjenaALaSesion() {
        Resena resenaTutor = Resena.crear(1L, completada, tutor, (short) 4, null, ahora);
        Resena resenaAprendiz = Resena.crear(2L, completada, aprendiz, (short) 5, null, ahora);

        assertThat(resenaTutor.autorRol()).isEqualTo(Resena.AutorRol.TUTOR);
        assertThat(resenaAprendiz.autorRol()).isEqualTo(Resena.AutorRol.APRENDIZ);
        assertThatThrownBy(() -> Resena.crear(
                3L, completada, persona(9L), (short) 4, null, ahora
        )).isInstanceOf(ResenaInvalidaException.class);
    }

    @Test
    void unaMismaSesionNoPuedeTenerDosResenasDelMismoRol() {
        Resena primera = resena(completada, Resena.AutorRol.TUTOR, (short) 4);
        Resena mismaSesionMismoRol = resena(
                completada, Resena.AutorRol.TUTOR, (short) 5
        );
        Resena otroRol = resena(completada, Resena.AutorRol.APRENDIZ, (short) 3);

        assertThatThrownBy(() -> primera.validarUnica(List.of(mismaSesionMismoRol)))
                .isInstanceOf(ResenaInvalidaException.class);
        primera.validarUnica(List.of(otroRol));
    }

    @Test
    void detectaDuplicadoAunqueLaSesionProvengaDeOtraInstancia() {
        Sesion mismaSesionPersistida = sesion(Sesion.EstadoSesion.COMPLETADA);
        Resena primera = resena(completada, Resena.AutorRol.APRENDIZ, (short) 4);
        Resena duplicada = resena(
                mismaSesionPersistida, Resena.AutorRol.APRENDIZ, (short) 3
        );

        assertThatThrownBy(() -> primera.validarUnica(List.of(duplicada)))
                .isInstanceOf(ResenaInvalidaException.class);
    }

    private Resena resena(Sesion sesion, Resena.AutorRol rol, short calificacion) {
        return new Resena(1L, sesion, rol, calificacion, null, ahora);
    }

    private Sesion sesion(Sesion.EstadoSesion estado) {
        OffsetDateTime confirmacionTutor =
                estado == Sesion.EstadoSesion.COMPLETADA ? ahora : null;
        OffsetDateTime confirmacionAprendiz =
                estado == Sesion.EstadoSesion.COMPLETADA ? ahora : null;
        return new Sesion(
                10L,
                new Oferta(
                        20L,
                        tutor,
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
                aprendiz,
                ahora,
                null,
                estado,
                confirmacionTutor,
                confirmacionAprendiz,
                null,
                0,
                ahora
        );
    }

    private Persona persona(Long id) {
        Persona persona = new Persona();
        persona.setId(id);
        return persona;
    }
}
