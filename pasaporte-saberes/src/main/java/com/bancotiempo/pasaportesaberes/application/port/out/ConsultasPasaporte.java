package com.bancotiempo.pasaportesaberes.application.port.out;

import com.bancotiempo.pasaportesaberes.domain.model.Rango;

import java.math.BigDecimal;

public interface ConsultasPasaporte {
    Estadisticas obtenerEstadisticas(Long personaId, Rango.EscalaRango escala);

    record Estadisticas(
            BigDecimal horas,
            BigDecimal rating,
            Integer aprendicesDistintos,
            Integer habilidades,
            BigDecimal asistencia
    ) {
    }
}
