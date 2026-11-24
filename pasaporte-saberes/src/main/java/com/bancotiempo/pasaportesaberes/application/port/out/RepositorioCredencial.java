package com.bancotiempo.pasaportesaberes.application.port.out;

import com.bancotiempo.pasaportesaberes.domain.model.Credencial;

import java.util.List;
import java.util.Optional;

public interface RepositorioCredencial {
    Optional<Credencial> buscarPorId(Long id);

    Optional<Credencial> buscarPorCodigo(String codigo);

    List<Credencial> listarPorPersona(Long personaId);

    Optional<Credencial> buscarPorPersonaYHabilidad(Long personaId, Long habilidadId);

    Credencial guardar(Credencial credencial);
}
