package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.ReporteInvalidoException;

import java.time.OffsetDateTime;

public record Reporte(
    Long id,
    Sesion sesion,
    Persona autor,
    Administrador revisor,
    String motivo,
    EstadoReporte estado,
    OffsetDateTime creadoEn
) {
    public enum EstadoReporte {
        ABIERTO,
        EN_REVISION,
        RESUELTO,
        DESCARTADO
    }

    public Reporte {
        if (sesion == null || autor == null || motivo == null || motivo.isBlank()
                || estado == null || creadoEn == null) {
            throw new ReporteInvalidoException(
                    "La sesión, autor, motivo, estado y fecha son obligatorios");
        }
        if (sesion.estado() != Sesion.EstadoSesion.DISPUTADA) {
            throw new ReporteInvalidoException(
                    "El reporte requiere una sesión DISPUTADA");
        }
        if (!esParticipante(autor, sesion)) {
            throw new ReporteInvalidoException(
                    "El autor del reporte debe participar en la sesión");
        }
        if (estado == EstadoReporte.ABIERTO && revisor != null) {
            throw new ReporteInvalidoException("Un reporte ABIERTO aún no tiene revisor");
        }
        if (estado != EstadoReporte.ABIERTO && revisor == null) {
            throw new ReporteInvalidoException(
                    "Un reporte revisado debe tener un administrador revisor");
        }
    }

    public Reporte iniciarRevision(Administrador administrador) {
        if (estado != EstadoReporte.ABIERTO) {
            throw new ReporteInvalidoException(
                    "Solo un reporte ABIERTO puede pasar a EN_REVISION");
        }
        if (administrador == null) {
            throw new ReporteInvalidoException("El administrador revisor es obligatorio");
        }
        return conEstado(EstadoReporte.EN_REVISION, administrador);
    }

    public Reporte resolver(Administrador administrador) {
        validarRevisor(administrador);
        return conEstado(EstadoReporte.RESUELTO, revisor);
    }

    public Reporte descartar(Administrador administrador) {
        validarRevisor(administrador);
        return conEstado(EstadoReporte.DESCARTADO, revisor);
    }

    private void validarRevisor(Administrador administrador) {
        if (estado != EstadoReporte.EN_REVISION) {
            throw new ReporteInvalidoException(
                    "Solo un reporte EN_REVISION puede resolverse o descartarse");
        }
        if (!mismoAdministrador(revisor, administrador)) {
            throw new ReporteInvalidoException(
                    "Solo el administrador asignado puede cerrar el reporte");
        }
    }

    private Reporte conEstado(EstadoReporte nuevoEstado, Administrador nuevoRevisor) {
        return new Reporte(
                id,
                sesion,
                autor,
                nuevoRevisor,
                motivo,
                nuevoEstado,
                creadoEn
        );
    }

    private static boolean esParticipante(Persona persona, Sesion sesion) {
        return mismaPersona(persona, sesion.oferta().tutor())
                || mismaPersona(persona, sesion.aprendiz());
    }

    private static boolean mismaPersona(Persona primera, Persona segunda) {
        if (primera == null || segunda == null) {
            return false;
        }
        if (primera == segunda) {
            return true;
        }
        return primera.getId() != null && primera.getId().equals(segunda.getId());
    }

    private static boolean mismoAdministrador(
            Administrador primero,
            Administrador segundo
    ) {
        if (primero == null || segundo == null) {
            return false;
        }
        if (primero == segundo) {
            return true;
        }
        return primero.getId() != null && primero.getId().equals(segundo.getId());
    }
}
