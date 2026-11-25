package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioReporte;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioSesion;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.domain.model.Administrador;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import com.bancotiempo.pasaportesaberes.domain.model.Reporte;
import com.bancotiempo.pasaportesaberes.domain.model.Sesion;
import com.bancotiempo.pasaportesaberes.domain.model.Usuario;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Repository
@Transactional
public class RepositorioReporteJpa implements RepositorioReporte {
    private final ReporteJpaRepository reportes;
    private final RepositorioSesion sesiones;
    private final RepositorioUsuario usuarios;

    public RepositorioReporteJpa(
            ReporteJpaRepository reportes,
            RepositorioSesion sesiones,
            RepositorioUsuario usuarios
    ) {
        this.reportes = reportes;
        this.sesiones = sesiones;
        this.usuarios = usuarios;
    }

    @Override
    public Optional<Reporte> buscarPorId(Long id) {
        return reportes.findByIdForUpdate(id).map(this::aDominio);
    }

    @Override
    public Reporte guardar(Reporte reporte) {
        Objects.requireNonNull(reporte, "El reporte es obligatorio");
        ReporteJpaEntity entidad = reporte.id() == null
                ? new ReporteJpaEntity()
                : reportes.findByIdForUpdate(reporte.id()).orElseThrow(
                () -> new IllegalStateException(
                        "No existe el reporte que se quiere actualizar"));
        entidad.setSesionId(reporte.sesion().id());
        entidad.setAutorId(reporte.autor().getId());
        entidad.setRevisorId(
                reporte.revisor() == null ? null : reporte.revisor().getId());
        entidad.setMotivo(reporte.motivo());
        entidad.setEstado(reporte.estado());
        entidad.setCreadoEn(reporte.creadoEn());
        return aDominio(reportes.saveAndFlush(entidad));
    }

    private Reporte aDominio(ReporteJpaEntity entidad) {
        Sesion sesion = sesiones.buscarPorId(entidad.getSesionId()).orElseThrow(
                () -> new IllegalStateException(
                        "El reporte referencia una sesión inexistente"));
        Persona autor = buscarPersona(entidad.getAutorId());
        Administrador revisor = entidad.getRevisorId() == null
                ? null
                : buscarAdministrador(entidad.getRevisorId());
        return new Reporte(
                entidad.getId(),
                sesion,
                autor,
                revisor,
                entidad.getMotivo(),
                entidad.getEstado(),
                entidad.getCreadoEn()
        );
    }

    private Persona buscarPersona(Long id) {
        Usuario usuario = usuarios.buscarPorId(id).orElseThrow(
                () -> new IllegalStateException(
                        "El reporte referencia un autor inexistente"));
        if (!(usuario instanceof Persona persona)) {
            throw new IllegalStateException("El autor del reporte debe ser una persona");
        }
        return persona;
    }

    private Administrador buscarAdministrador(Long id) {
        Usuario usuario = usuarios.buscarPorId(id).orElseThrow(
                () -> new IllegalStateException(
                        "El reporte referencia un administrador inexistente"));
        if (!(usuario instanceof Administrador administrador)) {
            throw new IllegalStateException("El revisor del reporte debe ser administrador");
        }
        return administrador;
    }
}
