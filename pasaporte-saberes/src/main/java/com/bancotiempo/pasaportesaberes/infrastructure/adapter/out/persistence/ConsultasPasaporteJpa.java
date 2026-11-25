package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.application.port.out.ConsultasPasaporte;
import com.bancotiempo.pasaportesaberes.domain.model.Rango;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Repository
@Transactional(readOnly = true)
public class ConsultasPasaporteJpa implements ConsultasPasaporte {
    private final EntityManager entityManager;

    public ConsultasPasaporteJpa(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Estadisticas obtenerEstadisticas(Long personaId, Rango.EscalaRango escala) {
        Objects.requireNonNull(personaId, "La persona es obligatoria");
        Objects.requireNonNull(escala, "La escala de rango es obligatoria");
        Object[] datos = (Object[]) entityManager.createNativeQuery("""
                        SELECT COALESCE(SUM(o.duracion_horas), 0),
                               COUNT(DISTINCT o.habilidad_id)
                        FROM sesion s
                        JOIN oferta o ON o.id = s.oferta_id
                        WHERE s.estado = 'COMPLETADA'
                          AND (
                            (:escala = 'ENSENANZA' AND o.tutor_id = :personaId)
                            OR
                            (:escala = 'APRENDIZAJE' AND s.aprendiz_id = :personaId)
                          )
                        """)
                .setParameter("personaId", personaId)
                .setParameter("escala", escala.name())
                .getSingleResult();
        BigDecimal horas = (BigDecimal) datos[0];
        int habilidades = Math.toIntExact(((Number) datos[1]).longValue());
        BigDecimal rating = escala == Rango.EscalaRango.ENSENANZA
                ? obtenerRatingPonderado(personaId)
                : null;
        int aprendicesDistintos = escala == Rango.EscalaRango.ENSENANZA
                ? contarAprendicesDistintos(personaId)
                : 0;
        BigDecimal asistencia = obtenerAsistencia(personaId, escala);
        return new Estadisticas(
                horas.setScale(2, RoundingMode.HALF_UP),
                rating,
                aprendicesDistintos,
                habilidades,
                asistencia
        );
    }

    private BigDecimal obtenerRatingPonderado(Long personaId) {
        Object resultado = entityManager.createNativeQuery("""
                        WITH resenas_ordenadas AS (
                            SELECT r.calificacion,
                                   ROW_NUMBER() OVER (
                                       PARTITION BY s.aprendiz_id
                                       ORDER BY r.creada_en, r.id
                                   ) AS numero_resena
                            FROM resena r
                            JOIN sesion s ON s.id = r.sesion_id
                            JOIN oferta o ON o.id = s.oferta_id
                            WHERE o.tutor_id = :personaId
                              AND s.estado = 'COMPLETADA'
                              AND r.autor_rol = 'APRENDIZ'
                        )
                        SELECT ROUND(
                            SUM(calificacion * CASE
                                WHEN numero_resena = 1 THEN 1.00
                                WHEN numero_resena = 2 THEN 0.50
                                ELSE 0.25
                            END)
                            / NULLIF(SUM(CASE
                                WHEN numero_resena = 1 THEN 1.00
                                WHEN numero_resena = 2 THEN 0.50
                                ELSE 0.25
                            END), 0),
                            2
                        )
                        FROM resenas_ordenadas
                        """)
                .setParameter("personaId", personaId)
                .getSingleResult();
        return resultado == null ? null : (BigDecimal) resultado;
    }

    private int contarAprendicesDistintos(Long personaId) {
        Number cantidad = (Number) entityManager.createNativeQuery("""
                        SELECT COUNT(DISTINCT s.aprendiz_id)
                        FROM sesion s
                        JOIN oferta o ON o.id = s.oferta_id
                        WHERE o.tutor_id = :personaId
                          AND s.estado = 'COMPLETADA'
                        """)
                .setParameter("personaId", personaId)
                .getSingleResult();
        return Math.toIntExact(cantidad.longValue());
    }

    private BigDecimal obtenerAsistencia(Long personaId, Rango.EscalaRango escala) {
        Object resultado = entityManager.createNativeQuery("""
                        SELECT COALESCE(
                            ROUND(
                                SUM(CASE
                                    WHEN s.estado = 'COMPLETADA'
                                      OR s.estado = 'NO_ASISTIO'
                                         AND s.inasistente IS DISTINCT FROM :inasistente
                                    THEN 1.0
                                    ELSE 0.0
                                END)
                                / NULLIF(COUNT(*), 0),
                                3
                            ),
                            0.000
                        )
                        FROM sesion s
                        JOIN oferta o ON o.id = s.oferta_id
                        WHERE s.estado IN ('COMPLETADA', 'NO_ASISTIO')
                          AND (
                            (:escala = 'ENSENANZA' AND o.tutor_id = :personaId)
                            OR
                            (:escala = 'APRENDIZAJE' AND s.aprendiz_id = :personaId)
                          )
                        """)
                .setParameter(
                        "inasistente",
                        escala == Rango.EscalaRango.ENSENANZA ? "TUTOR" : "APRENDIZ")
                .setParameter("personaId", personaId)
                .setParameter("escala", escala.name())
                .getSingleResult();
        return (BigDecimal) resultado;
    }
}
