package com.bancotiempo.pasaportesaberes.application.service;

import com.bancotiempo.pasaportesaberes.application.ErrorAplicacionException;
import com.bancotiempo.pasaportesaberes.application.port.in.InstitucionesUseCases;
import com.bancotiempo.pasaportesaberes.application.port.out.NotificadorInstitucional;
import com.bancotiempo.pasaportesaberes.application.port.out.Reloj;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioAval;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioCredencial;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.application.port.out.UnidadDeTrabajo;
import com.bancotiempo.pasaportesaberes.domain.model.Administrador;
import com.bancotiempo.pasaportesaberes.domain.model.Aval;
import com.bancotiempo.pasaportesaberes.domain.model.Credencial;
import com.bancotiempo.pasaportesaberes.domain.model.EntidadAliada;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;

import java.time.LocalDate;
import java.util.Objects;

public final class InstitucionesService implements InstitucionesUseCases {
    private final RepositorioUsuario usuarios;
    private final RepositorioCredencial credenciales;
    private final RepositorioAval avales;
    private final NotificadorInstitucional notificador;
    private final UnidadDeTrabajo unidadDeTrabajo;
    private final Reloj reloj;

    public InstitucionesService(
            RepositorioUsuario usuarios,
            RepositorioCredencial credenciales,
            RepositorioAval avales,
            NotificadorInstitucional notificador,
            UnidadDeTrabajo unidadDeTrabajo,
            Reloj reloj
    ) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.credenciales = Objects.requireNonNull(credenciales);
        this.avales = Objects.requireNonNull(avales);
        this.notificador = Objects.requireNonNull(notificador);
        this.unidadDeTrabajo = Objects.requireNonNull(unidadDeTrabajo);
        this.reloj = Objects.requireNonNull(reloj);
    }

    @Override
    public void solicitarAval(SolicitudAval solicitud) {
        if (solicitud == null || solicitud.personaId() == null
                || solicitud.credencialId() == null || solicitud.entidadId() == null) {
            throw error(ErrorAplicacionException.Codigo.VALIDACION,
                    "La persona, credencial y entidad son obligatorias");
        }
        unidadDeTrabajo.ejecutar(() -> {
            Persona persona = buscarPersona(solicitud.personaId());
            Credencial credencial = buscarCredencial(solicitud.credencialId());
            if (!Objects.equals(credencial.persona().getId(), persona.getId())) {
                throw noAutorizado("La credencial no pertenece a la persona");
            }
            EntidadAliada entidad = buscarEntidad(solicitud.entidadId());
            notificador.notificarSolicitudAval(
                    entidad.getCorreo(),
                    persona.getNombre(),
                    credencial.codigoVerificacion()
            );
            return null;
        });
    }

    @Override
    public Aval registrarAval(RegistrarAval solicitud) {
        if (solicitud == null || solicitud.administradorId() == null
                || solicitud.entidadId() == null || solicitud.credencialId() == null
                || solicitud.fecha() == null || solicitud.vigenciaHasta() == null) {
            throw error(ErrorAplicacionException.Codigo.VALIDACION,
                    "Los datos del aval están incompletos");
        }
        return unidadDeTrabajo.ejecutar(() -> {
            buscarAdministrador(solicitud.administradorId());
            EntidadAliada entidad = buscarEntidad(solicitud.entidadId());
            Credencial credencial = buscarCredencial(solicitud.credencialId());
            Aval existente = avales.buscarPorEntidadYCredencial(
                    entidad.getId(), credencial.id()).orElse(null);
            if (existente != null && existente.estado() == Aval.EstadoAval.VIGENTE) {
                throw error(ErrorAplicacionException.Codigo.CONFLICTO,
                        "La entidad ya emitió un aval vigente para esta credencial");
            }
            LocalDate hoy = reloj.hoy();
            if (solicitud.fecha().isAfter(hoy)) {
                throw error(ErrorAplicacionException.Codigo.VALIDACION,
                        "La fecha de emisión del aval no puede ser futura");
            }
            Aval aval = new Aval(
                    null,
                    entidad,
                    credencial,
                    solicitud.fecha(),
                    solicitud.vigenciaHasta(),
                    Aval.EstadoAval.VIGENTE
            );
            return avales.guardar(aval);
        });
    }

    private Persona buscarPersona(Long id) {
        return usuarios.buscarPorId(id)
                .filter(Persona.class::isInstance)
                .map(Persona.class::cast)
                .orElseThrow(() -> noEncontrado("No se encontró la persona"));
    }

    private Administrador buscarAdministrador(Long id) {
        return usuarios.buscarPorId(id)
                .filter(Administrador.class::isInstance)
                .map(Administrador.class::cast)
                .orElseThrow(() -> noAutorizado("El usuario no es administrador"));
    }

    private EntidadAliada buscarEntidad(Long id) {
        return usuarios.buscarPorId(id)
                .filter(EntidadAliada.class::isInstance)
                .map(EntidadAliada.class::cast)
                .orElseThrow(() -> noEncontrado("No se encontró la entidad aliada"));
    }

    private Credencial buscarCredencial(Long id) {
        return credenciales.buscarPorId(id)
                .orElseThrow(() -> noEncontrado("No se encontró la credencial"));
    }

    private static ErrorAplicacionException noEncontrado(String mensaje) {
        return error(ErrorAplicacionException.Codigo.NO_ENCONTRADO, mensaje);
    }

    private static ErrorAplicacionException noAutorizado(String mensaje) {
        return error(ErrorAplicacionException.Codigo.NO_AUTORIZADO, mensaje);
    }

    private static ErrorAplicacionException error(
            ErrorAplicacionException.Codigo codigo,
            String mensaje
    ) {
        return new ErrorAplicacionException(codigo, mensaje);
    }
}
