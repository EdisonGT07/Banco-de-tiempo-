package com.bancotiempo.pasaportesaberes.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Evidencia(
    Long id,
    Sesion sesion,
    String tokenHash,
    String codigoCortoHash,
    OffsetDateTime expiraEn,
    OffsetDateTime usadoEn,
    short intentosFallidos,
    String fotoUrl,
    BigDecimal latitud,
    BigDecimal longitud,
    OffsetDateTime marcaServidor
) {
}
