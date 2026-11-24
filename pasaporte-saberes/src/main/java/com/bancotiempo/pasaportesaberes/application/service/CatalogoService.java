package com.bancotiempo.pasaportesaberes.application.service;

import com.bancotiempo.pasaportesaberes.application.ErrorAplicacionException;
import com.bancotiempo.pasaportesaberes.application.port.in.CatalogoUseCases;
import com.bancotiempo.pasaportesaberes.application.port.out.Reloj;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioHabilidad;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioOferta;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioQuererAprender;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioSesion;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.application.port.out.UnidadDeTrabajo;
import com.bancotiempo.pasaportesaberes.domain.model.Habilidad;
import com.bancotiempo.pasaportesaberes.domain.model.Oferta;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import com.bancotiempo.pasaportesaberes.domain.model.QuererAprender;
import com.bancotiempo.pasaportesaberes.domain.model.QuererAprenderId;
import com.bancotiempo.pasaportesaberes.domain.model.Sesion;

import java.util.Objects;

public final class CatalogoService implements CatalogoUseCases {
    private final RepositorioUsuario usuarios;
    private final RepositorioHabilidad habilidades;
    private final RepositorioOferta ofertas;
    private final RepositorioSesion sesiones;
    private final RepositorioQuererAprender intereses;
    private final UnidadDeTrabajo unidadDeTrabajo;
    private final Reloj reloj;

    public CatalogoService(
            RepositorioUsuario usuarios,
            RepositorioHabilidad habilidades,
            RepositorioOferta ofertas,
            RepositorioSesion sesiones,
            RepositorioQuererAprender intereses,
            UnidadDeTrabajo unidadDeTrabajo,
            Reloj reloj
    ) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.habilidades = Objects.requireNonNull(habilidades);
        this.ofertas = Objects.requireNonNull(ofertas);
        this.sesiones = Objects.requireNonNull(sesiones);
        this.intereses = Objects.requireNonNull(intereses);
        this.unidadDeTrabajo = Objects.requireNonNull(unidadDeTrabajo);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Override
    public Oferta publicarOferta(PublicarOferta solicitud) {
        if (solicitud == null) {
            throw error(ErrorAplicacionException.Codigo.VALIDACION,
                    "Los datos de la oferta son obligatorios");
        }
        return unidadDeTrabajo.ejecutar(() -> {
            Persona tutor = buscarPersona(solicitud.tutorId());
            tutor.validarPuedeParticipar(reloj.ahora());
            Habilidad habilidad = habilidades.buscarPorId(solicitud.habilidadId())
                    .orElseThrow(() -> noEncontrado("No se encontró la habilidad"));
            Persona receptor = solicitud.receptorRegaloId() == null
                    ? null
                    : buscarPersona(solicitud.receptorRegaloId());
            Oferta oferta = new Oferta(
                    null,
                    tutor,
                    habilidad,
                    solicitud.descripcion(),
                    solicitud.duracionHoras(),
                    solicitud.modalidad(),
                    solicitud.barrio(),
                    solicitud.destinoHoras(),
                    receptor,
                    true,
                    reloj.ahora()
            );
            return ofertas.guardar(oferta);
        });
    }

    @Override
    public Sesion crearSesion(CrearSesion solicitud) {
        if (solicitud == null || solicitud.iniciaEn() == null
                || !solicitud.iniciaEn().isAfter(reloj.ahora())) {
            throw error(ErrorAplicacionException.Codigo.VALIDACION,
                    "La sesión debe programarse para una fecha futura");
        }
        return unidadDeTrabajo.ejecutar(() -> {
            Persona actor = buscarPersona(solicitud.tutorId());
            actor.validarPuedeParticipar(reloj.ahora());
            Oferta oferta = ofertas.buscarPorId(solicitud.ofertaId())
                    .orElseThrow(() -> noEncontrado("No se encontró la oferta"));
            if (!mismaPersona(actor, oferta.tutor())) {
                throw error(ErrorAplicacionException.Codigo.NO_AUTORIZADO,
                        "Solo el tutor dueño puede crear sesiones para su oferta");
            }
            if (!oferta.activa()) {
                throw error(ErrorAplicacionException.Codigo.CONFLICTO,
                        "No se pueden crear sesiones desde una oferta inactiva");
            }
            return sesiones.guardar(new Sesion(
                    null,
                    oferta,
                    null,
                    solicitud.iniciaEn(),
                    solicitud.lugarOEnlace(),
                    Sesion.EstadoSesion.PROGRAMADA,
                    null,
                    null,
                    null,
                    0,
                    reloj.ahora()
            ));
        });
    }

    @Override
    public QuererAprender registrarSolicitudAprendizaje(
            RegistrarSolicitudAprendizaje solicitud
    ) {
        if (solicitud == null) {
            throw error(ErrorAplicacionException.Codigo.VALIDACION,
                    "Los datos de la solicitud son obligatorios");
        }
        return unidadDeTrabajo.ejecutar(() -> {
            Persona persona = buscarPersona(solicitud.personaId());
            persona.validarPuedeParticipar(reloj.ahora());
            Habilidad habilidad = habilidades.buscarPorId(solicitud.habilidadId())
                    .orElseThrow(() -> noEncontrado("No se encontró la habilidad"));
            QuererAprenderId id = new QuererAprenderId(persona.getId(), habilidad.id());
            if (intereses.buscarPorId(id).isPresent()) {
                throw error(ErrorAplicacionException.Codigo.CONFLICTO,
                        "La persona ya registró que quiere aprender esta habilidad");
            }
            return intereses.guardar(QuererAprender.crear(persona, habilidad, reloj.ahora()));
        });
    }

    private Persona buscarPersona(Long id) {
        return usuarios.buscarPorId(id)
                .filter(Persona.class::isInstance)
                .map(Persona.class::cast)
                .orElseThrow(() -> noEncontrado("No se encontró la persona"));
    }

    private static boolean mismaPersona(Persona primera, Persona segunda) {
        return primera == segunda
                || primera != null && segunda != null
                && primera.getId() != null && primera.getId().equals(segunda.getId());
    }

    private static ErrorAplicacionException noEncontrado(String mensaje) {
        return error(ErrorAplicacionException.Codigo.NO_ENCONTRADO, mensaje);
    }

    private static ErrorAplicacionException error(
            ErrorAplicacionException.Codigo codigo,
            String mensaje
    ) {
        return new ErrorAplicacionException(codigo, mensaje);
    }
}
