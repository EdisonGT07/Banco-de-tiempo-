package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.CredencialInvalidaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CredencialTest {
    private final OffsetDateTime ahora = OffsetDateTime.parse("2026-11-24T10:00:00Z");
    private final LocalDate fecha = ahora.toLocalDate();
    private final Persona persona = persona(1L);
    private final Habilidad habilidad = new Habilidad(1L, "Habilidad", "General");
    private final Credencial credencial = credencial(1L, "2.00", "4.00");

    @Test
    void validaAtributosYEstadisticasDerivadas() {
        Credencial actualizada = credencial.actualizarEstadisticas(
                new BigDecimal("5.50"),
                new BigDecimal("4.25"),
                "firma-nueva",
                ahora.plusDays(1)
        );
        Credencial verificada = actualizada.registrarVerificacion(ahora.plusDays(2));

        assertThat(actualizada.horasEnsenadas()).isEqualByComparingTo("5.50");
        assertThat(actualizada.ratingPromedio()).isEqualByComparingTo("4.25");
        assertThat(credencial.horasEnsenadas()).isEqualByComparingTo("2.00");
        assertThat(verificada.numeroVerificaciones()).isEqualTo(1);
        assertThat(verificada.actualizadaEn()).isEqualTo(ahora.plusDays(2));
    }

    @Test
    void rechazaHorasYRatingsFueraDeLosLimitesDeLaBase() {
        assertThatThrownBy(() -> credencial(2L, "-1", "4.00"))
                .isInstanceOf(CredencialInvalidaException.class);
        assertThatThrownBy(() -> credencial(3L, "1000000.00", "4.00"))
                .isInstanceOf(CredencialInvalidaException.class);
        assertThatThrownBy(() -> credencial(4L, "1.00", "5.01"))
                .isInstanceOf(CredencialInvalidaException.class);
        assertThatThrownBy(() -> credencial(5L, "1.00", "4.001"))
                .isInstanceOf(CredencialInvalidaException.class);
    }

    @Test
    void calculaNivelRegistradoValidadoYAvaladoConUmbralesRecibidos() {
        assertThat(credencial.calcularNivel(
                2, new BigDecimal("4.00"), 3, null, fecha
        )).isEqualTo(Credencial.Nivel.REGISTRADA);
        assertThat(credencial.calcularNivel(
                3, new BigDecimal("4.00"), 3, null, fecha
        )).isEqualTo(Credencial.Nivel.VALIDADA_POR_PARES);

        Aval aval = new Aval(
                1L,
                new EntidadAliada(),
                credencial,
                fecha.minusDays(1),
                fecha.plusDays(30),
                Aval.EstadoAval.VIGENTE
        );
        assertThat(credencial.calcularNivel(
                1, new BigDecimal("4.00"), 3, aval, fecha
        )).isEqualTo(Credencial.Nivel.AVALADA);
        assertThat(credencial.calcularNivel(
                3, new BigDecimal("4.00"), 3, aval.revocar(), fecha
        )).isEqualTo(Credencial.Nivel.VALIDADA_POR_PARES);
    }

    @Test
    void calculaRatingPonderadoLimitandoElPesoDeUnMismoAprendiz() {
        Persona aprendizUno = persona(2L);
        Persona aprendizDos = persona(3L);
        Habilidad otraHabilidad = new Habilidad(2L, "Otra habilidad", "General");
        Persona otroTutor = persona(4L);
        List<Resena> resenas = List.of(
                resena(1L, persona, aprendizUno, otraHabilidad, (short) 2, ahora),
                resena(2L, persona, aprendizUno, habilidad, (short) 5, ahora.plusDays(1)),
                resena(3L, persona, aprendizUno, habilidad, (short) 1, ahora.plusDays(2)),
                resena(4L, persona, aprendizDos, habilidad, (short) 3, ahora.plusDays(3)),
                resena(5L, otroTutor, aprendizDos, habilidad, (short) 5, ahora.plusDays(4))
        );

        assertThat(credencial.calcularRatingPonderado(resenas))
                .isEqualByComparingTo("3.29");
    }

    @Test
    void rechazaAvalDeOtraCredencialYArgumentosInvalidos() {
        Credencial otra = credencial(2L, "2.00", "4.00");
        Aval avalAjeno = new Aval(
                1L,
                new EntidadAliada(),
                otra,
                fecha.minusDays(1),
                fecha.plusDays(30),
                Aval.EstadoAval.VIGENTE
        );

        assertThatThrownBy(() -> credencial.calcularNivel(
                3, new BigDecimal("4.00"), 3, avalAjeno, fecha
        )).isInstanceOf(CredencialInvalidaException.class);
        assertThatThrownBy(() -> credencial.calcularNivel(
                -1, new BigDecimal("4.00"), 3, null, fecha
        )).isInstanceOf(CredencialInvalidaException.class);
    }

    private Credencial credencial(Long id, String horas, String rating) {
        return new Credencial(
                id,
                persona,
                habilidad,
                new BigDecimal(horas),
                rating == null ? null : new BigDecimal(rating),
                "codigo-" + id,
                "firma",
                ahora,
                0
        );
    }

    private Resena resena(
            Long sesionId,
            Persona tutor,
            Persona aprendiz,
            Habilidad habilidad,
            short calificacion,
            OffsetDateTime creadaEn
    ) {
        Oferta oferta = new Oferta(
                sesionId,
                tutor,
                habilidad,
                "Oferta",
                BigDecimal.ONE,
                Oferta.Modalidad.VIRTUAL,
                null,
                Oferta.DestinoHoras.BILLETERA_PROPIA,
                null,
                true,
                creadaEn
        );
        Sesion sesion = new Sesion(
                sesionId,
                oferta,
                aprendiz,
                creadaEn,
                null,
                Sesion.EstadoSesion.COMPLETADA,
                creadaEn,
                creadaEn,
                null,
                0,
                creadaEn
        );
        return new Resena(
                sesionId,
                sesion,
                Resena.AutorRol.APRENDIZ,
                calificacion,
                null,
                creadaEn
        );
    }

    private Persona persona(Long id) {
        Persona persona = new Persona();
        persona.setId(id);
        return persona;
    }
}
