package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AvalJpaRepository extends JpaRepository<AvalJpaEntity, Long> {
    List<AvalJpaEntity> findAllByCredencialId(Long credencialId);

    Optional<AvalJpaEntity> findByEntidadIdAndCredencialId(
            Long entidadId,
            Long credencialId
    );
}
