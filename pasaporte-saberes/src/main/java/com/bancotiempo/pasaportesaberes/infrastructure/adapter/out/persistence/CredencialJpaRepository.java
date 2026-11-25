package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CredencialJpaRepository
        extends JpaRepository<CredencialJpaEntity, Long> {
    Optional<CredencialJpaEntity> findByCodigoVerificacion(String codigoVerificacion);

    List<CredencialJpaEntity> findAllByPersonaId(Long personaId);

    Optional<CredencialJpaEntity> findByPersonaIdAndHabilidadId(
            Long personaId,
            Long habilidadId
    );
}
