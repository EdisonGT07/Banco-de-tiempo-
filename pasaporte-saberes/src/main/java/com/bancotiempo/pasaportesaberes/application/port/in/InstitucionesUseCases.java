package com.bancotiempo.pasaportesaberes.application.port.in;

import com.bancotiempo.pasaportesaberes.domain.model.Aval;

import java.time.LocalDate;

public interface InstitucionesUseCases {
    void solicitarAval(SolicitudAval solicitud);

    Aval registrarAval(RegistrarAval solicitud);

    record SolicitudAval(Long personaId, Long credencialId, Long entidadId) {
    }

    record RegistrarAval(
            Long administradorId,
            Long entidadId,
            Long credencialId,
            LocalDate fecha,
            LocalDate vigenciaHasta
    ) {
    }
}
