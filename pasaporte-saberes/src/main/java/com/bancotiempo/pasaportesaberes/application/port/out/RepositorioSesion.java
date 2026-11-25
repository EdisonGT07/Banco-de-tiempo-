package com.bancotiempo.pasaportesaberes.application.port.out;

import com.bancotiempo.pasaportesaberes.domain.model.Sesion;

import java.util.List;
import java.util.Optional;

public interface RepositorioSesion {
    Optional<Sesion> buscarPorId(Long id);

    Optional<Sesion> bloquearPorId(Long id);

    List<Sesion> listarPorPersona(Long personaId);

    List<Sesion> listarCompletadasPorTutor(Long tutorId);

    Sesion guardar(Sesion sesion);
}
