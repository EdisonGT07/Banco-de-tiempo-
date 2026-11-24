package com.bancotiempo.pasaportesaberes.application.port.out;

import com.bancotiempo.pasaportesaberes.domain.model.Aval;

import java.util.List;
import java.util.Optional;

public interface RepositorioAval {
    List<Aval> listarPorCredencial(Long credencialId);

    Optional<Aval> buscarPorEntidadYCredencial(Long entidadId, Long credencialId);

    Aval guardar(Aval aval);
}
