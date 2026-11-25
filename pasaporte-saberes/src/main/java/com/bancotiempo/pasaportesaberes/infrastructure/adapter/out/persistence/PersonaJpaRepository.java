package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonaJpaRepository extends JpaRepository<PersonaJpaEntity, Long> {
    boolean existsByDocHmac(String docHmac);
}
