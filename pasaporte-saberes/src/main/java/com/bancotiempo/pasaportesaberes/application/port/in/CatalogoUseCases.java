package com.bancotiempo.pasaportesaberes.application.port.in;

import com.bancotiempo.pasaportesaberes.domain.model.Oferta;
import com.bancotiempo.pasaportesaberes.domain.model.QuererAprender;
import com.bancotiempo.pasaportesaberes.domain.model.Sesion;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public interface CatalogoUseCases {
    Oferta publicarOferta(PublicarOferta solicitud);

    List<HabilidadPublica> listarHabilidades();

    HabilidadPublica registrarHabilidad(NuevaHabilidad solicitud);

    Sesion crearSesion(CrearSesion solicitud);

    QuererAprender registrarSolicitudAprendizaje(RegistrarSolicitudAprendizaje solicitud);

    record HabilidadPublica(Long id, String nombre, String categoria) {
    }

    record NuevaHabilidad(String nombre, String categoria) {
    }

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
