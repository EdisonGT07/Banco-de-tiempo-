package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.ReporteInvalidoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReporteTest {
    private final OffsetDateTime ahora = OffsetDateTime.parse("2026-11-24T10:00:00Z");
    private final Persona tutor = persona(1L);
    private final Persona aprendiz = persona(2L);
    private final Persona ajeno = persona(3L);
    private final Administrador revisor = administrador(4L);
    private final Reporte reporte = new Reporte(
            1L,
            sesionDisputada(),
            aprendiz,
            null,
            "Problema durante la sesión",
            Reporte.EstadoReporte.ABIERTO,
            ahora
    );

    @Test
    void autorDebeSerParticipanteYLaSesionDebeEstarDisputada() {
        assertThatThrownBy(() -> new Reporte(
                2L,
                sesionDisputada(),
                ajeno,
                null,
                "Motivo",
                Reporte.EstadoReporte.ABIERTO,
                ahora
        )).isInstanceOf(ReporteInvalidoException.class);
        assertThatThrownBy(() -> new Reporte(
                3L,
                sesion(Sesion.EstadoSesion.COMPLETADA),
                aprendiz,
                null,
                "Motivo",
                Reporte.EstadoReporte.ABIERTO,
                ahora
        )).isInstanceOf(ReporteInvalidoException.class);
    }

    @Test
    void reportePasaDeAbiertoARevisionYAEstadoFinal() {
        Reporte enRevision = reporte.iniciarRevision(revisor);
        Reporte resuelto = enRevision.resolver(revisor);

        assertThat(enRevision.estado()).isEqualTo(Reporte.EstadoReporte.EN_REVISION);
        assertThat(enRevision.revisor()).isSameAs(revisor);
        assertThat(resuelto.estado()).isEqualTo(Reporte.EstadoReporte.RESUELTO);
        assertThat(reporte.estado()).isEqualTo(Reporte.EstadoReporte.ABIERTO);
        assertThatThrownBy(() -> enRevision.descartar(administrador(5L)))
                .isInstanceOf(ReporteInvalidoException.class);
    }

    @Test
    void soloUnReporteEnRevisionPuedeDescartarse() {
        Reporte enRevision = reporte.iniciarRevision(revisor);
        Reporte descartado = enRevision.descartar(revisor);

        assertThat(descartado.estado()).isEqualTo(Reporte.EstadoReporte.DESCARTADO);
        assertThatThrownBy(() -> reporte.resolver(revisor))
                .isInstanceOf(ReporteInvalidoException.class);
        assertThatThrownBy(() -> reporte.iniciarRevision(null))
                .isInstanceOf(ReporteInvalidoException.class);
    }

    private Sesion sesionDisputada() {
        return sesion(Sesion.EstadoSesion.DISPUTADA);
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

    private Administrador administrador(Long id) {
        Administrador administrador = new Administrador();
        administrador.setId(id);
        return administrador;
    }
}
