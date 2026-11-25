package com.bancotiempo.pasaportesaberes.application.port.out;

import com.bancotiempo.pasaportesaberes.domain.model.Habilidad;

import java.util.List;
import java.util.Optional;

public interface RepositorioHabilidad {
    Optional<Habilidad> buscarPorId(Long id);

    List<Habilidad> listarTodas();

    Habilidad guardar(Habilidad habilidad);
}
