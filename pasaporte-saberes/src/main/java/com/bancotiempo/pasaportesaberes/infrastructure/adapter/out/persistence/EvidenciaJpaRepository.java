package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface EvidenciaJpaRepository
        extends JpaRepository<EvidenciaJpaEntity, Long> {
    Optional<EvidenciaJpaEntity> findBySesionId(Long sesionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EvidenciaJpaEntity e where e.id = :id")
    Optional<EvidenciaJpaEntity> findByIdForUpdate(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE evidencia
            SET usado_en = :ahora
            WHERE sesion_id = :sesionId
              AND usado_en IS NULL
              AND expira_en > :ahora
              AND intentos_fallidos < 5
              AND (token_hash = :hash OR codigo_corto_hash = :hash)
            """, nativeQuery = true)
    int consumirSiUsable(
            @Param("sesionId") Long sesionId,
            @Param("hash") String hash,
            @Param("ahora") OffsetDateTime ahora
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE evidencia
            SET intentos_fallidos = intentos_fallidos + 1
            WHERE sesion_id = :sesionId
              AND usado_en IS NULL
              AND expira_en > :ahora
              AND intentos_fallidos < 5
            """, nativeQuery = true)
    int registrarIntentoFallidoSiUsable(
            @Param("sesionId") Long sesionId,
            @Param("ahora") OffsetDateTime ahora
    );
}
