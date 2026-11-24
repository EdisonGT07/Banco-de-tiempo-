package com.bancotiempo.pasaportesaberes.application.port.in;

import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import com.bancotiempo.pasaportesaberes.domain.model.Reporte;

import java.math.BigDecimal;

public interface AdministracionUseCases {
    PanelImpacto obtenerPanelImpacto();

    Reporte revisarReporte(RevisionReporte solicitud);

    Persona desbloquearUsuario(DesbloquearUsuario solicitud);

    record PanelImpacto(
            BigDecimal horasIntercambiadas,
            long personasFormadas,
            long habilidadesNuevas,
            long credencialesEmitidas,
            long verificaciones
    ) {
    }

    record RevisionReporte(
            Long administradorId,
            Long reporteId,
            boolean resolver
    ) {
    }

    record DesbloquearUsuario(Long administradorId, Long personaId) {
    }
}
