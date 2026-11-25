package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HabilidadJpaRepository extends JpaRepository<HabilidadJpaEntity, Long> {
    List<HabilidadJpaEntity> findAllByOrderByNombre();
}
