package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface TransaccionJpaRepository
        extends JpaRepository<TransaccionJpaEntity, Long> {
    Optional<TransaccionJpaEntity> findByClaveIdempotencia(String claveIdempotencia);

    @Query(value = """
            SELECT COALESCE(SUM(t.horas), 0)
            FROM transaccion t
            JOIN billetera b ON b.id = t.billetera_destino_id
            WHERE t.tipo = 'RETIRO_FONDO'
              AND b.persona_id = :personaId
              AND t.creada_en >= :inicio
              AND t.creada_en < :fin
            """, nativeQuery = true)
    BigDecimal sumarRetiros(
            @Param("personaId") Long personaId,
            @Param("inicio") OffsetDateTime inicio,
            @Param("fin") OffsetDateTime fin
    );
}
