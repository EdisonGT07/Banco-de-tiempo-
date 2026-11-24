package com.bancotiempo.pasaportesaberes.application.port.out;

import com.bancotiempo.pasaportesaberes.domain.model.Oferta;

import java.util.List;
import java.util.Optional;

public interface RepositorioOferta {
    Optional<Oferta> buscarPorId(Long id);

    List<Oferta> listarPorTutor(Long tutorId);

    Oferta guardar(Oferta oferta);
}
