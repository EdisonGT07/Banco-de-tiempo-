package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuererAprenderJpaRepository
        extends JpaRepository<QuererAprenderJpaEntity, QuererAprenderJpaId> {
    List<QuererAprenderJpaEntity> findAllByIdPersonaIdOrderByCreadaEn(
            Long personaId
    );
}
