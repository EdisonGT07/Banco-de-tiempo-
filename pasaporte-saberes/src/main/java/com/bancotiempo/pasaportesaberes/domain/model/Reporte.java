package com.bancotiempo.pasaportesaberes.domain.model;

import java.time.OffsetDateTime;

public record Reporte(
    Long id,
    Sesion sesion,
    Persona autor,
    Administrador revisor,
    String motivo,
    EstadoReporte estado,
    OffsetDateTime creadoEn
) {
    public enum EstadoReporte {
        ABIERTO,
        EN_REVISION,
        RESUELTO,
        DESCARTADO
    }
}
