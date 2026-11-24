package com.bancotiempo.pasaportesaberes.application.port.out;

import com.bancotiempo.pasaportesaberes.domain.model.Evidencia;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface RepositorioEvidencia {
    Optional<Evidencia> buscarPorSesion(Long sesionId);

    Evidencia guardar(Evidencia evidencia);

    Optional<Evidencia> consumirSiUsable(
            Long sesionId,
            String hashPresentado,
            OffsetDateTime ahora
    );

    boolean registrarIntentoFallidoSiUsable(Long sesionId, OffsetDateTime ahora);
}
