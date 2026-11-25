package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.application.port.out.UnidadDeTrabajo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;
import java.util.function.Supplier;

@Component
public final class UnidadDeTrabajoJpa implements UnidadDeTrabajo {
    private final TransactionTemplate transacciones;

    public UnidadDeTrabajoJpa(PlatformTransactionManager transactionManager) {
        this.transacciones = new TransactionTemplate(Objects.requireNonNull(transactionManager));
    }

    @Override
    public <T> T ejecutar(Supplier<T> operacion) {
        Objects.requireNonNull(operacion, "La operación es obligatoria");
        return transacciones.execute(status -> operacion.get());
    }
}
