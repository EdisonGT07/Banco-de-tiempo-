package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioRango;
import com.bancotiempo.pasaportesaberes.domain.model.Rango;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public class RepositorioRangoJpa implements RepositorioRango {
    private final RangoJpaRepository rangos;

    public RepositorioRangoJpa(RangoJpaRepository rangos) {
        this.rangos = rangos;
    }

    @Override
    public List<Rango> listarPorEscala(Rango.EscalaRango escala) {
        return rangos.findAllByEscalaOrderByOrden(escala).stream()
                .map(RepositorioRangoJpa::aDominio)
                .toList();
    }

    static Rango aDominio(RangoJpaEntity rango) {
        return new Rango(
                rango.getId(),
                rango.getEscala(),
                rango.getNombre(),
                rango.getOrden(),
                rango.getMinimoHoras(),
                rango.getMinimoRating(),
                rango.getMinimoAprendices(),
                rango.getMinimoHabilidades(),
                rango.getMinimoAsistencia()
        );
    }
}
