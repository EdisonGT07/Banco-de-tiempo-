package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReporteJpaRepository extends JpaRepository<ReporteJpaEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ReporteJpaEntity r where r.id = :id")
    Optional<ReporteJpaEntity> findByIdForUpdate(@Param("id") Long id);
}
