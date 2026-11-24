package com.bancotiempo.pasaportesaberes.application.port.out;

import com.bancotiempo.pasaportesaberes.domain.model.Reporte;

import java.util.Optional;

public interface RepositorioReporte {
    Optional<Reporte> buscarPorId(Long id);

    Reporte guardar(Reporte reporte);
}
