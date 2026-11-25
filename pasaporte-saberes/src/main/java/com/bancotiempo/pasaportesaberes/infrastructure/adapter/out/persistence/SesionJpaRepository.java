package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SesionJpaRepository extends JpaRepository<SesionJpaEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SesionJpaEntity s where s.id = :id")
    Optional<SesionJpaEntity> findByIdForUpdate(@Param("id") Long id);

    @Query(value = """
            SELECT s.*
            FROM sesion s
            JOIN oferta o ON o.id = s.oferta_id
            WHERE s.aprendiz_id = :personaId OR o.tutor_id = :personaId
            ORDER BY s.inicia_en
            """, nativeQuery = true)
    List<SesionJpaEntity> findAllForPersona(@Param("personaId") Long personaId);

    @Query(value = """
            SELECT s.*
            FROM sesion s
            JOIN oferta o ON o.id = s.oferta_id
            WHERE o.tutor_id = :tutorId AND s.estado = 'COMPLETADA'
            ORDER BY s.inicia_en
            """, nativeQuery = true)
    List<SesionJpaEntity> findAllCompletadasByTutor(@Param("tutorId") Long tutorId);
}
