package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioResena;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioSesion;
import com.bancotiempo.pasaportesaberes.domain.model.Resena;
import com.bancotiempo.pasaportesaberes.domain.model.Sesion;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Repository
@Transactional
public class RepositorioResenaJpa implements RepositorioResena {
    private final ResenaJpaRepository resenas;
    private final RepositorioSesion sesiones;

    public RepositorioResenaJpa(
            ResenaJpaRepository resenas,
            RepositorioSesion sesiones
    ) {
        this.resenas = resenas;
        this.sesiones = sesiones;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Resena> listarPorSesion(Long sesionId) {
        return resenas.findAllBySesionIdOrderByCreadaEn(sesionId).stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Resena> listarRecibidasPorTutor(Long tutorId) {
        return resenas.findAllRecibidasPorTutor(tutorId).stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    public Resena guardar(Resena resena) {
        Objects.requireNonNull(resena, "La reseña es obligatoria");
        ResenaJpaEntity entidad = resena.id() == null
                ? new ResenaJpaEntity()
                : resenas.findById(resena.id()).orElseThrow(
                () -> new IllegalStateException(
                        "No existe la reseña que se quiere actualizar"));
        entidad.setSesionId(resena.sesion().id());
        entidad.setAutorRol(resena.autorRol());
        entidad.setCalificacion(resena.calificacion());
        entidad.setComentario(resena.comentario());
        entidad.setCreadaEn(resena.creadaEn());
        return aDominio(resenas.saveAndFlush(entidad));
    }

    private Resena aDominio(ResenaJpaEntity entidad) {
        Sesion sesion = sesiones.buscarPorId(entidad.getSesionId()).orElseThrow(
                () -> new IllegalStateException(
                        "La reseña referencia una sesión inexistente"));
        return new Resena(
                entidad.getId(),
                sesion,
                entidad.getAutorRol(),
                entidad.getCalificacion(),
                entidad.getComentario(),
                entidad.getCreadaEn()
        );
    }
}
