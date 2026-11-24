package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.SesionInvalidaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SesionTest {
    private final OffsetDateTime ahora = OffsetDateTime.parse("2026-11-24T10:00:00Z");
    private final Persona tutor = persona(1L);
    private final Persona aprendiz = persona(2L);
    private final Persona ajeno = persona(3L);
    private final Oferta oferta = new Oferta(
            1L,
            tutor,
            new Habilidad(1L, "Habilidad", "General"),
            "Oferta de prueba",
            BigDecimal.ONE,
            Oferta.Modalidad.VIRTUAL,
            null,
            Oferta.DestinoHoras.BILLETERA_PROPIA,
            null,
            true,
            ahora
    );

    @Test
    void reservarAsignaAprendizDistintoDelTutor() {
        Sesion reservada = programada().reservar(aprendiz);

        assertThat(reservada.estado()).isEqualTo(Sesion.EstadoSesion.RESERVADA);
        assertThat(reservada.aprendiz()).isSameAs(aprendiz);
        assertThat(reservada.version()).isEqualTo(1);
        assertThatThrownBy(() -> programada().reservar(tutor))
                .isInstanceOf(SesionInvalidaException.class);
    }

    @Test
    void handshakeSoloIniciaLaSesionCuandoLoConfirmaElAprendiz() {
        Sesion enCurso = reservada().confirmarHandshake(aprendiz);

        assertThat(enCurso.estado()).isEqualTo(Sesion.EstadoSesion.EN_CURSO);
        assertThat(enCurso.version()).isEqualTo(2);
        assertThatThrownBy(() -> reservada().confirmarHandshake(tutor))
                .isInstanceOf(SesionInvalidaException.class);
    }

    @Test
    void ambasConfirmacionesCompletanLaSesion() {
        Sesion pendiente = enCurso().confirmarFin(tutor, ahora);
        Sesion completada = pendiente.confirmarFin(aprendiz, ahora.plusMinutes(2));

        assertThat(pendiente.estado())
                .isEqualTo(Sesion.EstadoSesion.PENDIENTE_CONFIRMACION);
        assertThat(completada.estado()).isEqualTo(Sesion.EstadoSesion.COMPLETADA);
        assertThat(completada.finConfirmacionTutor()).isEqualTo(ahora);
        assertThat(completada.finConfirmacionAprendiz()).isEqualTo(ahora.plusMinutes(2));
    }

    @Test
    void repetirConfirmacionDelMismoParticipanteNoCambiaLaSesion() {
        Sesion pendiente = enCurso().confirmarFin(tutor, ahora);

        Sesion repetida = pendiente.confirmarFin(tutor, ahora.plusMinutes(10));

        assertThat(repetida).isSameAs(pendiente);
        assertThat(repetida.finConfirmacionTutor()).isEqualTo(ahora);
    }

    @Test
    void participantesAjenoNoPuedeConfirmarElFin() {
        assertThatThrownBy(() -> enCurso().confirmarFin(ajeno, ahora))
                .isInstanceOf(SesionInvalidaException.class);
    }

    @Test
    void tutorOAprendizPuedeCancelarAntesDeLaHoraProgramada() {
        Sesion canceladaPorTutor = programada().cancelar(tutor, ahora);
        Sesion canceladaPorAprendiz = reservada().cancelar(aprendiz, ahora);

        assertThat(canceladaPorTutor.estado()).isEqualTo(Sesion.EstadoSesion.CANCELADA);
        assertThat(canceladaPorAprendiz.estado()).isEqualTo(Sesion.EstadoSesion.CANCELADA);
        assertThatThrownBy(() -> reservada().cancelar(ajeno, ahora))
                .isInstanceOf(SesionInvalidaException.class);
    }

    @Test
    void noPermiteCancelarAlLlegarLaHoraProgramadaNiDespuesDeIniciar() {
        assertThatThrownBy(() -> reservada().cancelar(tutor, programada().iniciaEn()))
                .isInstanceOf(SesionInvalidaException.class);
        assertThatThrownBy(() -> enCurso().cancelar(tutor, ahora))
                .isInstanceOf(SesionInvalidaException.class);
    }

    @Test
    void inasistenciaRequierePlazoVencidoYReporteDelParticipanteQueAsistio() {
        Sesion reservada = reservada();
        OffsetDateTime limite = reservada.iniciaEn().plusMinutes(30);

        assertThatThrownBy(() -> reservada.marcarInasistencia(
                aprendiz, Sesion.Inasistente.TUTOR, limite, Duration.ofMinutes(30)
        )).isInstanceOf(SesionInvalidaException.class);

        Sesion noAsistio = reservada.marcarInasistencia(
                aprendiz,
                Sesion.Inasistente.TUTOR,
                limite.plusNanos(1),
                Duration.ofMinutes(30)
        );
        assertThat(noAsistio.estado()).isEqualTo(Sesion.EstadoSesion.NO_ASISTIO);
        assertThat(noAsistio.inasistente()).isEqualTo(Sesion.Inasistente.TUTOR);
    }

    @Test
    void participanteNoPuedeReportarseComoInasistente() {
        assertThatThrownBy(() -> reservada().marcarInasistencia(
                tutor,
                Sesion.Inasistente.TUTOR,
                programada().iniciaEn().plusHours(1),
                Duration.ofMinutes(30)
        )).isInstanceOf(SesionInvalidaException.class);
    }

    @Test
    void expiracionCuentaDesdeLaPrimeraConfirmacion() {
        Sesion pendiente = enCurso().confirmarFin(tutor, ahora);
        OffsetDateTime limite = ahora.plusHours(72);

        assertThatThrownBy(() -> pendiente.expirar(limite, Duration.ofHours(72)))
                .isInstanceOf(SesionInvalidaException.class);

        Sesion expirada = pendiente.expirar(limite.plusNanos(1), Duration.ofHours(72));
        assertThat(expirada.estado()).isEqualTo(Sesion.EstadoSesion.EXPIRADA);
        assertThat(expirada.finConfirmacionTutor()).isEqualTo(ahora);
    }

    @Test
    void participantesPuedenDisputarUnaSesionActiva() {
        Sesion disputada = reservada().disputar(aprendiz);

        assertThat(disputada.estado()).isEqualTo(Sesion.EstadoSesion.DISPUTADA);
        assertThatThrownBy(() -> completada().disputar(tutor))
                .isInstanceOf(SesionInvalidaException.class);
    }

    @Test
    void constructorRechazaInvariantesInvalidas() {
        assertThatThrownBy(() -> new Sesion(
                1L, oferta, tutor, ahora, null, Sesion.EstadoSesion.RESERVADA,
                null, null, null, 0, ahora
        )).isInstanceOf(SesionInvalidaException.class);

        assertThatThrownBy(() -> new Sesion(
                1L, oferta, aprendiz, ahora, null, Sesion.EstadoSesion.COMPLETADA,
                ahora, null, null, 0, ahora
        )).isInstanceOf(SesionInvalidaException.class);

        assertThatThrownBy(() -> new Sesion(
                1L, oferta, aprendiz, ahora, null, Sesion.EstadoSesion.NO_ASISTIO,
                null, null, null, 0, ahora
        )).isInstanceOf(SesionInvalidaException.class);
    }

    private Sesion programada() {
        return new Sesion(
                10L,
                oferta,
                null,
                ahora.plusHours(1),
                null,
                Sesion.EstadoSesion.PROGRAMADA,
                null,
                null,
                null,
                0,
                ahora
        );
    }

    private Sesion reservada() {
        return programada().reservar(aprendiz);
    }

    private Sesion enCurso() {
        return reservada().confirmarHandshake(aprendiz);
    }

    private Sesion completada() {
        return enCurso()
                .confirmarFin(tutor, ahora)
                .confirmarFin(aprendiz, ahora.plusMinutes(1));
    }

    private Persona persona(Long id) {
        Persona persona = new Persona();
        persona.setId(id);
        return persona;
    }
}
