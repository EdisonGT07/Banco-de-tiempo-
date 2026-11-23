package com.bancotiempo.pasaportesaberes.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Credencial(
    Long id,
    Persona persona,
    Habilidad habilidad,
    BigDecimal horasEnsenadas,
    BigDecimal ratingPromedio,
    String codigoVerificacion,
    String firma,
    OffsetDateTime actualizadaEn,
    long numeroVerificaciones
) {
}
