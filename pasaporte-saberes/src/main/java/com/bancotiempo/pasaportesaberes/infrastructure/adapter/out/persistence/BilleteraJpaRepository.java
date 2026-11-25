package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.domain.model.Billetera;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BilleteraJpaRepository
        extends JpaRepository<BilleteraJpaEntity, Long> {
    Optional<BilleteraJpaEntity> findByPersonaId(Long personaId);

    Optional<BilleteraJpaEntity> findByTipo(Billetera.TipoBilletera tipo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<BilleteraJpaEntity> findByIdInOrderByIdAsc(Collection<Long> ids);
}
