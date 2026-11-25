package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.application.port.in.AgoraUseCases;
import com.bancotiempo.pasaportesaberes.application.port.out.ConsultasAgora;
import com.bancotiempo.pasaportesaberes.application.port.out.Reloj;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
@Transactional(readOnly = true)
public class ConsultasAgoraJpa implements ConsultasAgora {
    private static final String NIVEL_CREDENCIAL_SQL = """
            CASE
                WHEN c.id IS NULL THEN NULL
                WHEN EXISTS (
                    SELECT 1
                    FROM aval a
                    WHERE a.credencial_id = c.id
                      AND a.estado = 'VIGENTE'
                      AND :fechaActual BETWEEN a.fecha AND a.vigencia_hasta
                ) THEN 'AVALADA'
                WHEN c.rating_promedio >= :ratingMinimo
                  AND (
                    SELECT COUNT(DISTINCT s.aprendiz_id)
                    FROM resena res
                    JOIN sesion s ON s.id = res.sesion_id
                    JOIN oferta ofr ON ofr.id = s.oferta_id
                    WHERE ofr.tutor_id = o.tutor_id
                      AND ofr.habilidad_id = o.habilidad_id
                      AND s.estado = 'COMPLETADA'
                      AND res.autor_rol = 'APRENDIZ'
                  ) >= :aprendicesMinimos
                THEN 'VALIDADA_POR_PARES'
                ELSE 'REGISTRADA'
            END
            """;

    private final EntityManager entityManager;
    private final Reloj reloj;
    private final BigDecimal ratingMinimo;
    private final int aprendicesMinimos;

    public ConsultasAgoraJpa(
            EntityManager entityManager,
            Reloj reloj,
            @Value("${pasaporte-saberes.pasaporte.rating-minimo-validacion}")
            BigDecimal ratingMinimo,
            @Value("${pasaporte-saberes.pasaporte.aprendices-minimos-validacion}")
            int aprendicesMinimos
    ) {
        this.entityManager = entityManager;
        this.reloj = reloj;
        this.ratingMinimo = ratingMinimo;
        this.aprendicesMinimos = aprendicesMinimos;
    }

    @Override
    public List<AgoraUseCases.TarjetaOferta> buscar(
            AgoraUseCases.FiltrosBusqueda filtros
    ) {
        Objects.requireNonNull(filtros, "Los filtros de búsqueda son obligatorios");
        StringBuilder sql = new StringBuilder("""
                SELECT o.id, o.tutor_id, u.nombre, h.nombre, o.descripcion,
                       o.duracion_horas, o.modalidad, o.barrio,
                       c.rating_promedio, r.nombre,
                """);
        sql.append(NIVEL_CREDENCIAL_SQL).append("""
                 FROM oferta o
                 JOIN persona p ON p.usuario_id = o.tutor_id
                 JOIN usuario u ON u.id = p.usuario_id
                 JOIN habilidad h ON h.id = o.habilidad_id
                 LEFT JOIN rango r ON r.id = p.rango_ens_id
                 LEFT JOIN credencial c
                   ON c.persona_id = o.tutor_id AND c.habilidad_id = o.habilidad_id
                 WHERE o.activa
                """);

        if (filtros.habilidadId() != null) {
            sql.append(" AND o.habilidad_id = :habilidadId");
        }
        if (filtros.modalidad() != null) {
            sql.append(" AND o.modalidad = :modalidad");
        }
        if (filtros.barrio() != null && !filtros.barrio().isBlank()) {
            sql.append(" AND LOWER(o.barrio) LIKE :barrio");
        }
        if (filtros.texto() != null && !filtros.texto().isBlank()) {
            sql.append(" AND LOWER(CONCAT_WS(' ', h.nombre, o.descripcion)) LIKE :texto");
        }
        if (filtros.calificacionMinima() != null) {
            sql.append(" AND c.rating_promedio >= :calificacionMinima");
        }
        if (filtros.ordenRangoMinimo() != null) {
            sql.append(" AND r.orden >= :ordenRangoMinimo");
        }
        sql.append("""
                 ORDER BY r.orden DESC NULLS LAST,
                          c.rating_promedio DESC NULLS LAST,
                          o.creada_en DESC, o.id
                """);

        Query consulta = entityManager.createNativeQuery(sql.toString());
        consulta.setParameter("fechaActual", reloj.hoy());
        consulta.setParameter("ratingMinimo", ratingMinimo);
        consulta.setParameter("aprendicesMinimos", aprendicesMinimos);
        if (filtros.habilidadId() != null) {
            consulta.setParameter("habilidadId", filtros.habilidadId());
        }
        if (filtros.modalidad() != null) {
            consulta.setParameter("modalidad", filtros.modalidad().name());
        }
        if (filtros.barrio() != null && !filtros.barrio().isBlank()) {
            consulta.setParameter("barrio", "%" + filtros.barrio().trim().toLowerCase() + "%");
        }
        if (filtros.texto() != null && !filtros.texto().isBlank()) {
            consulta.setParameter("texto", "%" + filtros.texto().trim().toLowerCase() + "%");
        }
        if (filtros.calificacionMinima() != null) {
            consulta.setParameter("calificacionMinima", filtros.calificacionMinima());
        }
        if (filtros.ordenRangoMinimo() != null) {
            consulta.setParameter("ordenRangoMinimo", filtros.ordenRangoMinimo());
        }

        int desde = Math.multiplyExact(filtros.pagina(), filtros.tamanoPagina());
        List<?> filas = consulta.setFirstResult(desde)
                .setMaxResults(filtros.tamanoPagina())
                .getResultList();
        List<AgoraUseCases.TarjetaOferta> ofertas = new ArrayList<>(filas.size());
        for (Object fila : filas) {
            Object[] columnas = (Object[]) fila;
            ofertas.add(new AgoraUseCases.TarjetaOferta(
                    numeroLong(columnas[0]),
                    numeroLong(columnas[1]),
                    (String) columnas[2],
                    (String) columnas[3],
                    (String) columnas[4],
                    (BigDecimal) columnas[5],
                    com.bancotiempo.pasaportesaberes.domain.model.Oferta.Modalidad.valueOf(
                            (String) columnas[6]),
                    (String) columnas[7],
                    (BigDecimal) columnas[8],
                    (String) columnas[9],
                    (String) columnas[10]
            ));
        }
        return List.copyOf(ofertas);
    }

    @Override
    public List<AgoraUseCases.SugerenciaEmparejamiento> sugerir(Long personaId) {
        Objects.requireNonNull(personaId, "La persona es obligatoria");
        List<?> filas = entityManager.createNativeQuery("""
                        SELECT DISTINCT :personaId, candidata.id, candidata.tutor_id,
                               candidata.habilidad_id, TRUE
                        FROM oferta candidata
                        JOIN querer_aprender interes_persona
                          ON interes_persona.persona_id = :personaId
                         AND interes_persona.habilidad_id = candidata.habilidad_id
                        WHERE candidata.activa
                          AND candidata.tutor_id <> :personaId
                          AND EXISTS (
                            SELECT 1
                            FROM oferta propia
                            JOIN querer_aprender interes_tutor
                              ON interes_tutor.persona_id = candidata.tutor_id
                             AND interes_tutor.habilidad_id = propia.habilidad_id
                            WHERE propia.tutor_id = :personaId
                              AND propia.activa
                              AND interes_tutor.habilidad_id = propia.habilidad_id
                          )
                        ORDER BY candidata.id
                        """)
                .setParameter("personaId", personaId)
                .getResultList();

        List<AgoraUseCases.SugerenciaEmparejamiento> sugerencias =
                new ArrayList<>(filas.size());
        for (Object fila : filas) {
            Object[] columnas = (Object[]) fila;
            sugerencias.add(new AgoraUseCases.SugerenciaEmparejamiento(
                    numeroLong(columnas[0]),
                    numeroLong(columnas[1]),
                    numeroLong(columnas[2]),
                    numeroLong(columnas[3]),
                    (Boolean) columnas[4]
            ));
        }
        return List.copyOf(sugerencias);
    }

    private static Long numeroLong(Object valor) {
        return ((Number) valor).longValue();
    }
}
