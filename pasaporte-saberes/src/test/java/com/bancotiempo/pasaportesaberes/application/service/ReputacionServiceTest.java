package com.bancotiempo.pasaportesaberes.application.service;

import com.bancotiempo.pasaportesaberes.application.port.in.PasaporteUseCases;
import com.bancotiempo.pasaportesaberes.application.port.out.Reloj;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioResena;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioSesion;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.application.port.out.UnidadDeTrabajo;
import com.bancotiempo.pasaportesaberes.domain.model.Habilidad;
import com.bancotiempo.pasaportesaberes.domain.model.Oferta;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import com.bancotiempo.pasaportesaberes.domain.model.Resena;
import com.bancotiempo.pasaportesaberes.domain.model.Sesion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReputacionServiceTest {
    private final OffsetDateTime ahora = OffsetDateTime.parse("2026-11-24T10:00:00Z");
    private final Persona tutor = persona(1L);
    private final Persona aprendizUno = persona(2L);
    private final Persona aprendizDos = persona(3L);
    private final Habilidad habilidad = new Habilidad(1L, "Habilidad", "General");
    private final Habilidad otraHabilidad = new Habilidad(2L, "Otra habilidad", "General");

    @Test
    void ponderaResenasDeLaHabilidadYConsideraHistorialDelMismoAprendiz() {
        RepositorioUsuario usuarios = mock(RepositorioUsuario.class);
        RepositorioSesion sesiones = mock(RepositorioSesion.class);
        RepositorioResena resenas = mock(RepositorioResena.class);
        Reloj reloj = mock(Reloj.class);
        PasaporteUseCases pasaporte = mock(PasaporteUseCases.class);
        when(usuarios.buscarPorId(tutor.getId())).thenReturn(Optional.of(tutor));
        when(resenas.listarRecibidasPorTutor(tutor.getId())).thenReturn(List.of(
                resena(1L, tutor, aprendizUno, otraHabilidad, 2, ahora),
                resena(2L, tutor, aprendizUno, habilidad, 5, ahora.plusDays(1)),
                resena(3L, tutor, aprendizUno, habilidad, 1, ahora.plusDays(2)),
                resena(4L, tutor, aprendizDos, habilidad, 3, ahora.plusDays(3)),
                resena(5L, persona(4L), aprendizDos, habilidad, 5, ahora.plusDays(4))
        ));

        ReputacionService servicio = new ReputacionService(
                usuarios,
                sesiones,
                resenas,
                new UnidadDeTrabajo() {
                    @Override
                    public <T> T ejecutar(Supplier<T> operacion) {
                        return operacion.get();
                    }
                },
                reloj,
                pasaporte
        );

        ReputacionService.Reputacion reputacion =
                servicio.obtenerReputacion(tutor.getId(), habilidad.id());

        assertThat(reputacion.promedioPonderado()).isEqualByComparingTo("3.29");
        assertThat(reputacion.aprendicesDistintos()).isEqualTo(2);
        assertThat(reputacion.cantidadResenas()).isEqualTo(3);
    }

    private Resena resena(
            Long sesionId,
            Persona quienEnsenya,
            Persona quienAprende,
            Habilidad tema,
            int calificacion,
            OffsetDateTime creadaEn
    ) {
        Oferta oferta = new Oferta(
                sesionId,
                quienEnsenya,
                tema,
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
                quienAprende,
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
                (short) calificacion,
                null,
                creadaEn
        );
    }

    private static Persona persona(Long id) {
        Persona persona = new Persona();
        persona.setId(id);
        return persona;
    }
}
