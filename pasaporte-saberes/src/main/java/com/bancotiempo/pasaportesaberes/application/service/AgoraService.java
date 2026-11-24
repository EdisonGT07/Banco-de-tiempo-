package com.bancotiempo.pasaportesaberes.application.service;

import com.bancotiempo.pasaportesaberes.application.ErrorAplicacionException;
import com.bancotiempo.pasaportesaberes.application.port.in.AgoraUseCases;
import com.bancotiempo.pasaportesaberes.application.port.out.ConsultasAgora;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;

import java.util.List;
import java.util.Objects;

public final class AgoraService implements AgoraUseCases {
    private final ConsultasAgora consultas;
    private final RepositorioUsuario usuarios;

    public AgoraService(ConsultasAgora consultas, RepositorioUsuario usuarios) {
        this.consultas = Objects.requireNonNull(consultas);
        this.usuarios = Objects.requireNonNull(usuarios);
    }

    @Override
    public List<TarjetaOferta> buscarOfertasEnAgora(FiltrosBusqueda filtros) {
        if (filtros == null || filtros.pagina() == null || filtros.tamanoPagina() == null
                || filtros.pagina() < 0 || filtros.tamanoPagina() <= 0) {
            throw error("La búsqueda requiere paginación válida");
        }
        return List.copyOf(consultas.buscar(filtros));
    }

    @Override
    public List<SugerenciaEmparejamiento> sugerirEmparejamientos(Long personaId) {
        usuarios.buscarPorId(personaId)
                .filter(Persona.class::isInstance)
                .orElseThrow(() -> new ErrorAplicacionException(
                        ErrorAplicacionException.Codigo.NO_ENCONTRADO,
                        "No se encontró la persona"));
        return List.copyOf(consultas.sugerir(personaId));
    }

    private static ErrorAplicacionException error(String mensaje) {
        return new ErrorAplicacionException(
                ErrorAplicacionException.Codigo.VALIDACION,
                mensaje
        );
    }
}
