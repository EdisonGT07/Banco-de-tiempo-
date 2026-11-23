package com.bancotiempo.pasaportesaberes.domain.model;

import java.time.LocalDate;

public record Aval(
    Long id,
    EntidadAliada entidad,
    Credencial credencial,
    LocalDate fecha,
    LocalDate vigenciaHasta,
    EstadoAval estado
) {
    public enum EstadoAval {
        VIGENTE,
        REVOCADO
    }
}
