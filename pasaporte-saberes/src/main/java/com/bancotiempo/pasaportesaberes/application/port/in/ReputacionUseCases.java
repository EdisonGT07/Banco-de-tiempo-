package com.bancotiempo.pasaportesaberes.application.port.in;

import com.bancotiempo.pasaportesaberes.domain.model.Resena;

import java.math.BigDecimal;

public interface ReputacionUseCases {
    Resena calificarSesion(CalificarSesion solicitud);

    Reputacion obtenerReputacion(Long tutorId, Long habilidadId);

    record CalificarSesion(
            Long autorId,
            Long sesionId,
            short calificacion,
            String comentario
    ) {
    }

    record Reputacion(
            BigDecimal promedioPonderado,
            int aprendicesDistintos,
            long cantidadResenas
    ) {
    }
}
