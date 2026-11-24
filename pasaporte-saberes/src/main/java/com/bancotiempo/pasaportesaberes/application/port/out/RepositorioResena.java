package com.bancotiempo.pasaportesaberes.application.port.out;

import com.bancotiempo.pasaportesaberes.domain.model.Resena;

import java.util.List;

public interface RepositorioResena {
    List<Resena> listarPorSesion(Long sesionId);

    List<Resena> listarRecibidasPorTutor(Long tutorId);

    Resena guardar(Resena resena);
}
