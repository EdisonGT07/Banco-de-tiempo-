package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResenaJpaRepository extends JpaRepository<ResenaJpaEntity, Long> {
    List<ResenaJpaEntity> findAllBySesionIdOrderByCreadaEn(Long sesionId);

    @Query(value = """
            SELECT r.*
            FROM resena r
            JOIN sesion s ON s.id = r.sesion_id
            JOIN oferta o ON o.id = s.oferta_id
            WHERE o.tutor_id = :tutorId AND r.autor_rol = 'APRENDIZ'
            ORDER BY r.creada_en
            """, nativeQuery = true)
    List<ResenaJpaEntity> findAllRecibidasPorTutor(@Param("tutorId") Long tutorId);
}
