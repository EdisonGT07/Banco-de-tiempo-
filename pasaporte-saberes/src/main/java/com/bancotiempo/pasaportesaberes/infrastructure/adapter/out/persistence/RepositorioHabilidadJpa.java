package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioHabilidad;
import com.bancotiempo.pasaportesaberes.domain.model.Habilidad;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class RepositorioHabilidadJpa implements RepositorioHabilidad {
    private final HabilidadJpaRepository habilidades;

    public RepositorioHabilidadJpa(HabilidadJpaRepository habilidades) {
        this.habilidades = habilidades;
    }

    @Override
    public Optional<Habilidad> buscarPorId(Long id) {
        return habilidades.findById(id).map(RepositorioHabilidadJpa::aDominio);
    }

    @Override
    public List<Habilidad> listarTodas() {
        return habilidades.findAllByOrderByNombre().stream()
                .map(RepositorioHabilidadJpa::aDominio)
                .toList();
    }

    @Override
    @Transactional
    public Habilidad guardar(Habilidad habilidad) {
        HabilidadJpaEntity guardada = habilidades.save(
                new HabilidadJpaEntity(habilidad.nombre(), habilidad.categoria()));
        return aDominio(guardada);
    }

    static Habilidad aDominio(HabilidadJpaEntity habilidad) {
        return new Habilidad(habilidad.getId(), habilidad.getNombre(), habilidad.getCategoria());
    }
}
