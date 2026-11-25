package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministradorJpaRepository
        extends JpaRepository<AdministradorJpaEntity, Long> {
}
