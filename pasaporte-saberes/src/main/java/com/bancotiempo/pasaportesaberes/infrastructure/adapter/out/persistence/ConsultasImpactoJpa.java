package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.application.port.in.AdministracionUseCases;
import com.bancotiempo.pasaportesaberes.application.port.out.ConsultasImpacto;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Repository
@Transactional(readOnly = true)
public class ConsultasImpactoJpa implements ConsultasImpacto {
    private final EntityManager entityManager;

    public ConsultasImpactoJpa(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public AdministracionUseCases.PanelImpacto obtenerPanel() {
        Object[] intercambio = (Object[]) entityManager.createNativeQuery("""
                        SELECT COALESCE(SUM(o.duracion_horas), 0),
                               COUNT(DISTINCT s.aprendiz_id),
                               COUNT(DISTINCT o.habilidad_id)
                        FROM sesion s
                        JOIN oferta o ON o.id = s.oferta_id
                        WHERE s.estado = 'COMPLETADA'
                        """)
                .getSingleResult();
        Object[] pasaporte = (Object[]) entityManager.createNativeQuery("""
                        SELECT COUNT(*), COALESCE(SUM(num_verificaciones), 0)
                        FROM credencial
                        """)
                .getSingleResult();
        return new AdministracionUseCases.PanelImpacto(
                (BigDecimal) intercambio[0],
                numeroLong(intercambio[1]),
                numeroLong(intercambio[2]),
                numeroLong(pasaporte[0]),
                numeroLong(pasaporte[1])
        );
    }

    private static long numeroLong(Object valor) {
        return ((Number) valor).longValue();
    }
}
