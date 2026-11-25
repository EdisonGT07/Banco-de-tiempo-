package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfertaJpaRepository extends JpaRepository<OfertaJpaEntity, Long> {
    List<OfertaJpaEntity> findAllByTutorId(Long tutorId);
}
