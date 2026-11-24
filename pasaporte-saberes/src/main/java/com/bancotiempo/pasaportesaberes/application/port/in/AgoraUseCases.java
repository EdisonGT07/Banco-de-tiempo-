package com.bancotiempo.pasaportesaberes.application.port.in;

import com.bancotiempo.pasaportesaberes.domain.model.Oferta;

import java.math.BigDecimal;
import java.util.List;

public interface AgoraUseCases {
    List<TarjetaOferta> buscarOfertasEnAgora(FiltrosBusqueda filtros);

    List<SugerenciaEmparejamiento> sugerirEmparejamientos(Long personaId);

    record FiltrosBusqueda(
            String texto,
            Long habilidadId,
            Oferta.Modalidad modalidad,
            String barrio,
            BigDecimal calificacionMinima,
            Short ordenRangoMinimo,
            Integer pagina,
            Integer tamanoPagina
    ) {
    }

    record TarjetaOferta(
            Long ofertaId,
            Long tutorId,
            String nombreTutor,
            String habilidad,
            String descripcion,
            BigDecimal duracionHoras,
            Oferta.Modalidad modalidad,
            String barrio,
            BigDecimal calificacion,
            String rango,
            String nivelCredencial
    ) {
    }

    record SugerenciaEmparejamiento(
            Long personaId,
            Long ofertaId,
            Long tutorId,
            Long habilidadId,
            boolean cruceMutuo
    ) {
    }
}
