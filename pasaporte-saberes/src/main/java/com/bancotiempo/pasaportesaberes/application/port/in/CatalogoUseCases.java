package com.bancotiempo.pasaportesaberes.application.port.in;

import com.bancotiempo.pasaportesaberes.domain.model.Oferta;
import com.bancotiempo.pasaportesaberes.domain.model.QuererAprender;
import com.bancotiempo.pasaportesaberes.domain.model.Sesion;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface CatalogoUseCases {
    Oferta publicarOferta(PublicarOferta solicitud);

    Sesion crearSesion(CrearSesion solicitud);

    QuererAprender registrarSolicitudAprendizaje(RegistrarSolicitudAprendizaje solicitud);

    record PublicarOferta(
            Long tutorId,
            Long habilidadId,
            String descripcion,
            BigDecimal duracionHoras,
            Oferta.Modalidad modalidad,
            String barrio,
            Oferta.DestinoHoras destinoHoras,
            Long receptorRegaloId
    ) {
    }

    record CrearSesion(
            Long tutorId,
            Long ofertaId,
            OffsetDateTime iniciaEn,
            String lugarOEnlace
    ) {
    }

    record RegistrarSolicitudAprendizaje(Long personaId, Long habilidadId) {
    }
}
