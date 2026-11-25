package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioOferta;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioSesion;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.domain.model.Oferta;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import com.bancotiempo.pasaportesaberes.domain.model.Sesion;
import com.bancotiempo.pasaportesaberes.domain.model.Usuario;
import jakarta.persistence.OptimisticLockException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@Transactional
public class RepositorioSesionJpa implements RepositorioSesion {
    private final SesionJpaRepository sesiones;
    private final RepositorioOferta ofertas;
    private final RepositorioUsuario usuarios;

    public RepositorioSesionJpa(
            SesionJpaRepository sesiones,
            RepositorioOferta ofertas,
            RepositorioUsuario usuarios
    ) {
        this.sesiones = sesiones;
        this.ofertas = ofertas;
        this.usuarios = usuarios;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sesion> buscarPorId(Long id) {
        return sesiones.findById(id).map(this::aDominio);
    }

    @Override
    public Optional<Sesion> bloquearPorId(Long id) {
        return sesiones.findByIdForUpdate(id).map(this::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sesion> listarPorPersona(Long personaId) {
        return sesiones.findAllForPersona(personaId).stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sesion> listarCompletadasPorTutor(Long tutorId) {
        return sesiones.findAllCompletadasByTutor(tutorId).stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public Sesion guardar(Sesion sesion) {
        Objects.requireNonNull(sesion, "La sesión es obligatoria");
        SesionJpaEntity entidad;
        if (sesion.id() == null) {
            if (sesion.version() != 0) {
                throw new OptimisticLockException(
                        "Una sesión nueva debe comenzar en la versión cero");
            }
            entidad = new SesionJpaEntity();
        } else {
            entidad = sesiones.findByIdForUpdate(sesion.id()).orElseThrow(
                    () -> new IllegalStateException("No existe la sesión que se quiere actualizar"));
            long versionPersistida = version(entidad);
            if (sesion.version() != versionPersistida
                    && sesion.version() != versionPersistida + 1) {
                throw new OptimisticLockException(
                        "La versión de la sesión no corresponde al estado persistido");
            }
        }

        entidad.setOfertaId(sesion.oferta().id());
        entidad.setAprendizId(
                sesion.aprendiz() == null ? null : sesion.aprendiz().getId());
        entidad.setIniciaEn(sesion.iniciaEn());
        entidad.setLugarOEnlace(sesion.lugarOEnlace());
        entidad.setEstado(sesion.estado());
        entidad.setFinConfirmacionTutor(sesion.finConfirmacionTutor());
        entidad.setFinConfirmacionAprendiz(sesion.finConfirmacionAprendiz());
        entidad.setInasistente(sesion.inasistente());
        entidad.setCreadaEn(sesion.creadaEn());
        return aDominio(sesiones.saveAndFlush(entidad));
    }

    private Sesion aDominio(SesionJpaEntity entidad) {
        Oferta oferta = ofertas.buscarPorId(entidad.getOfertaId()).orElseThrow(
                () -> new IllegalStateException("La sesión referencia una oferta inexistente"));
        Persona aprendiz = entidad.getAprendizId() == null
                ? null
                : buscarPersona(entidad.getAprendizId());
        return new Sesion(
                entidad.getId(),
                oferta,
                aprendiz,
                entidad.getIniciaEn(),
                entidad.getLugarOEnlace(),
                entidad.getEstado(),
                entidad.getFinConfirmacionTutor(),
                entidad.getFinConfirmacionAprendiz(),
                entidad.getInasistente(),
                version(entidad),
                entidad.getCreadaEn()
        );
    }

    private Persona buscarPersona(Long id) {
        Usuario usuario = usuarios.buscarPorId(id).orElseThrow(
                () -> new IllegalStateException("La sesión referencia un aprendiz inexistente"));
        if (!(usuario instanceof Persona persona)) {
            throw new IllegalStateException("El aprendiz de una sesión debe ser una persona");
        }
        return persona;
    }

    private static long version(SesionJpaEntity entidad) {
        return entidad.getVersion() == null ? 0 : entidad.getVersion();
    }
}
