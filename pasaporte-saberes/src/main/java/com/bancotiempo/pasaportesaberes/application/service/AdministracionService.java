package com.bancotiempo.pasaportesaberes.application.service;

import com.bancotiempo.pasaportesaberes.application.ErrorAplicacionException;
import com.bancotiempo.pasaportesaberes.application.port.in.AdministracionUseCases;
import com.bancotiempo.pasaportesaberes.application.port.out.ConsultasImpacto;
import com.bancotiempo.pasaportesaberes.application.port.out.DesbloqueadorPersona;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioReporte;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.application.port.out.UnidadDeTrabajo;
import com.bancotiempo.pasaportesaberes.domain.model.Administrador;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import com.bancotiempo.pasaportesaberes.domain.model.Reporte;

import java.util.Objects;

public final class AdministracionService implements AdministracionUseCases {
    private final RepositorioUsuario usuarios;
    private final RepositorioReporte reportes;
    private final ConsultasImpacto consultasImpacto;
    private final DesbloqueadorPersona desbloqueador;
    private final UnidadDeTrabajo unidadDeTrabajo;

    public AdministracionService(
            RepositorioUsuario usuarios,
            RepositorioReporte reportes,
            ConsultasImpacto consultasImpacto,
            DesbloqueadorPersona desbloqueador,
            UnidadDeTrabajo unidadDeTrabajo
    ) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.reportes = Objects.requireNonNull(reportes);
        this.consultasImpacto = Objects.requireNonNull(consultasImpacto);
        this.desbloqueador = Objects.requireNonNull(desbloqueador);
        this.unidadDeTrabajo = Objects.requireNonNull(unidadDeTrabajo);
    }

    @Override
    public PanelImpacto obtenerPanelImpacto() {
        PanelImpacto panel = consultasImpacto.obtenerPanel();
        if (panel == null) {
            throw new IllegalStateException("La consulta del panel de impacto devolvió null");
        }
        return panel;
    }

    @Override
    public Reporte revisarReporte(RevisionReporte solicitud) {
        if (solicitud == null || solicitud.administradorId() == null
                || solicitud.reporteId() == null) {
            throw error(ErrorAplicacionException.Codigo.VALIDACION,
                    "El administrador y el reporte son obligatorios");
        }
        return unidadDeTrabajo.ejecutar(() -> {
            Administrador administrador = buscarAdministrador(solicitud.administradorId());
            Reporte reporte = reportes.buscarPorId(solicitud.reporteId())
                    .orElseThrow(() -> noEncontrado("No se encontró el reporte"));
            if (reporte.estado() == Reporte.EstadoReporte.ABIERTO) {
                reporte = reporte.iniciarRevision(administrador);
            }
            Reporte cerrado = solicitud.resolver()
                    ? reporte.resolver(administrador)
                    : reporte.descartar(administrador);
            return reportes.guardar(cerrado);
        });
    }

    @Override
    public Persona desbloquearUsuario(DesbloquearUsuario solicitud) {
        if (solicitud == null || solicitud.administradorId() == null
                || solicitud.personaId() == null) {
            throw error(ErrorAplicacionException.Codigo.VALIDACION,
                    "El administrador y la persona son obligatorios");
        }
        return unidadDeTrabajo.ejecutar(() -> {
            buscarAdministrador(solicitud.administradorId());
            Persona persona = usuarios.buscarPorId(solicitud.personaId())
                    .filter(Persona.class::isInstance)
                    .map(Persona.class::cast)
                    .orElseThrow(() -> noEncontrado("No se encontró la persona"));
            return desbloqueador.desbloquear(persona.getId());
        });
    }

    private Administrador buscarAdministrador(Long id) {
        return usuarios.buscarPorId(id)
                .filter(Administrador.class::isInstance)
                .map(Administrador.class::cast)
                .orElseThrow(() -> error(
                        ErrorAplicacionException.Codigo.NO_AUTORIZADO,
                        "El usuario no es administrador"
                ));
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
