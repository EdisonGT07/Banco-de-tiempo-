package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioAval;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioCredencial;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.domain.model.Aval;
import com.bancotiempo.pasaportesaberes.domain.model.Credencial;
import com.bancotiempo.pasaportesaberes.domain.model.EntidadAliada;
import com.bancotiempo.pasaportesaberes.domain.model.Usuario;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@Transactional
public class RepositorioAvalJpa implements RepositorioAval {
    private final AvalJpaRepository avales;
    private final RepositorioUsuario usuarios;
    private final RepositorioCredencial credenciales;

    public RepositorioAvalJpa(
            AvalJpaRepository avales,
            RepositorioUsuario usuarios,
            RepositorioCredencial credenciales
    ) {
        this.avales = avales;
        this.usuarios = usuarios;
        this.credenciales = credenciales;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Aval> listarPorCredencial(Long credencialId) {
        return avales.findAllByCredencialId(credencialId).stream()
                .map(this::aDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Aval> buscarPorEntidadYCredencial(
            Long entidadId,
            Long credencialId
    ) {
        return avales.findByEntidadIdAndCredencialId(entidadId, credencialId)
                .map(this::aDominio);
    }

    @Override
    public Aval guardar(Aval aval) {
        Objects.requireNonNull(aval, "El aval es obligatorio");
        AvalJpaEntity entidad = aval.id() == null
                ? new AvalJpaEntity()
                : avales.findById(aval.id()).orElseThrow(
                () -> new IllegalStateException(
                        "No existe el aval que se quiere actualizar"));
        entidad.setEntidadId(aval.entidad().getId());
        entidad.setCredencialId(aval.credencial().id());
        entidad.setFecha(aval.fecha());
        entidad.setVigenciaHasta(aval.vigenciaHasta());
        entidad.setEstado(aval.estado());
        return aDominio(avales.saveAndFlush(entidad));
    }

    private Aval aDominio(AvalJpaEntity entidad) {
        Usuario usuario = usuarios.buscarPorId(entidad.getEntidadId()).orElseThrow(
                () -> new IllegalStateException(
                        "El aval referencia una entidad aliada inexistente"));
        if (!(usuario instanceof EntidadAliada entidadAliada)) {
            throw new IllegalStateException(
                    "El emisor de un aval debe ser una entidad aliada");
        }
        Credencial credencial = credenciales.buscarPorId(entidad.getCredencialId())
                .orElseThrow(() -> new IllegalStateException(
                        "El aval referencia una credencial inexistente"));
        return new Aval(
                entidad.getId(),
                entidadAliada,
                credencial,
                entidad.getFecha(),
                entidad.getVigenciaHasta(),
                entidad.getEstado()
        );
    }
}
