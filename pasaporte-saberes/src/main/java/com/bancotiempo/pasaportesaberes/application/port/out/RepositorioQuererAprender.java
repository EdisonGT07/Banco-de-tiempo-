package com.bancotiempo.pasaportesaberes.application.port.out;

import com.bancotiempo.pasaportesaberes.domain.model.QuererAprender;
import com.bancotiempo.pasaportesaberes.domain.model.QuererAprenderId;

import java.util.List;
import java.util.Optional;

public interface RepositorioQuererAprender {
    Optional<QuererAprender> buscarPorId(QuererAprenderId id);

    List<QuererAprender> listarPorPersona(Long personaId);

    QuererAprender guardar(QuererAprender quererAprender);

    void eliminar(QuererAprenderId id);
}
