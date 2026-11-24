package com.bancotiempo.pasaportesaberes.application.port.out;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface FirmadorCredenciales {
    String firmar(
            Long personaId,
            Long habilidadId,
            BigDecimal horasEnsenadas,
            BigDecimal ratingPromedio,
            String codigoVerificacion,
            OffsetDateTime actualizadaEn
    );
}
