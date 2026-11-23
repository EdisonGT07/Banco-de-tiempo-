package com.bancotiempo.pasaportesaberes.domain.model;

import java.time.OffsetDateTime;

public record Sesion(
    Long id,
    Oferta oferta,
    Persona aprendiz,
    OffsetDateTime iniciaEn,
    String lugarOEnlace,
    EstadoSesion estado,
    OffsetDateTime finConfirmacionTutor,
    OffsetDateTime finConfirmacionAprendiz,
    Inasistente inasistente,
    long version,
    OffsetDateTime creadaEn
) {
    public enum EstadoSesion {
        PROGRAMADA,
        RESERVADA,
        EN_CURSO,
        PENDIENTE_CONFIRMACION,
        COMPLETADA,
        CANCELADA,
        NO_ASISTIO,
        EXPIRADA,
        DISPUTADA
    }

    public enum Inasistente {
        TUTOR,
        APRENDIZ
    }
}
