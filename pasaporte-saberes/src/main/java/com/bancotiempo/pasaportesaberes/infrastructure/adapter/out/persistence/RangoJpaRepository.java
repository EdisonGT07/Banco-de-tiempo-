package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.domain.model.Rango;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RangoJpaRepository extends JpaRepository<RangoJpaEntity, Long> {
    List<RangoJpaEntity> findAllByEscalaOrderByOrden(Rango.EscalaRango escala);
}
