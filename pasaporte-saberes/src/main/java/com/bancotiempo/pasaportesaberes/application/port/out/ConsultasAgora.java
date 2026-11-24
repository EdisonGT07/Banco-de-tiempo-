package com.bancotiempo.pasaportesaberes.application.port.out;

import com.bancotiempo.pasaportesaberes.application.port.in.AgoraUseCases;

import java.util.List;

public interface ConsultasAgora {
    List<AgoraUseCases.TarjetaOferta> buscar(AgoraUseCases.FiltrosBusqueda filtros);

    List<AgoraUseCases.SugerenciaEmparejamiento> sugerir(Long personaId);
}
