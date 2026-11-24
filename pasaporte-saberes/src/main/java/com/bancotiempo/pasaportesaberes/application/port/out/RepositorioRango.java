package com.bancotiempo.pasaportesaberes.application.port.out;

import com.bancotiempo.pasaportesaberes.domain.model.Rango;

import java.util.List;

public interface RepositorioRango {
    List<Rango> listarPorEscala(Rango.EscalaRango escala);
}
