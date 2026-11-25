package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioBilletera;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioSesion;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioTransaccion;
import com.bancotiempo.pasaportesaberes.domain.model.Billetera;
import com.bancotiempo.pasaportesaberes.domain.model.Sesion;
import com.bancotiempo.pasaportesaberes.domain.model.Transaccion;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;

@Repository
@Transactional
public class RepositorioTransaccionJpa implements RepositorioTransaccion {
    private final TransaccionJpaRepository transacciones;
    private final RepositorioBilletera billeteras;
    private final RepositorioSesion sesiones;

    public RepositorioTransaccionJpa(
            TransaccionJpaRepository transacciones,
            RepositorioBilletera billeteras,
            RepositorioSesion sesiones
    ) {
        this.transacciones = transacciones;
        this.billeteras = billeteras;
        this.sesiones = sesiones;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Transaccion> buscarPorClaveIdempotencia(String clave) {
        return transacciones.findByClaveIdempotencia(clave).map(this::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumarRetiros(Long personaId, YearMonth periodo) {
        Objects.requireNonNull(personaId, "La persona es obligatoria");
        Objects.requireNonNull(periodo, "El período es obligatorio");
        OffsetDateTime inicio = periodo.atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime fin = periodo.plusMonths(1)
                .atDay(1)
                .atStartOfDay()
                .atOffset(ZoneOffset.UTC);
        return transacciones.sumarRetiros(personaId, inicio, fin);
    }

    @Override
    public Transaccion guardar(Transaccion transaccion) {
        Objects.requireNonNull(transaccion, "La transacción es obligatoria");
        if (transaccion.id() != null) {
            throw new IllegalArgumentException(
                    "El libro contable solo permite insertar transacciones nuevas");
        }
        TransaccionJpaEntity entidad = new TransaccionJpaEntity();
        entidad.setTipo(transaccion.tipo());
        entidad.setHoras(transaccion.horas());
        entidad.setBilleteraOrigenId(requerirId(transaccion.billeteraOrigen()));
        entidad.setBilleteraDestinoId(requerirId(transaccion.billeteraDestino()));
        entidad.setSesionId(
                transaccion.sesion() == null ? null : transaccion.sesion().id());
        entidad.setClaveIdempotencia(transaccion.claveIdempotencia());
        entidad.setCreadaEn(transaccion.creadaEn());
        TransaccionJpaEntity guardada = transacciones.saveAndFlush(entidad);
        return new Transaccion(
                guardada.getId(),
                transaccion.tipo(),
                transaccion.horas(),
                transaccion.billeteraOrigen(),
                transaccion.billeteraDestino(),
                transaccion.sesion(),
                transaccion.claveIdempotencia(),
                transaccion.creadaEn()
        );
    }

    private Transaccion aDominio(TransaccionJpaEntity entidad) {
        Billetera origen = billeteras.buscarPorId(entidad.getBilleteraOrigenId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "La transacción referencia una billetera de origen inexistente"));
        Billetera destino = billeteras.buscarPorId(entidad.getBilleteraDestinoId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "La transacción referencia una billetera de destino inexistente"));
        Sesion sesion = entidad.getSesionId() == null
                ? null
                : sesiones.buscarPorId(entidad.getSesionId()).orElseThrow(
                () -> new EntityNotFoundException(
                        "La transacción referencia una sesión inexistente"));
        return new Transaccion(
                entidad.getId(),
                entidad.getTipo(),
                entidad.getHoras(),
                origen,
                destino,
                sesion,
                entidad.getClaveIdempotencia(),
                entidad.getCreadaEn()
        );
    }

    private static Long requerirId(Billetera billetera) {
        if (billetera.getId() == null) {
            throw new IllegalStateException(
                    "Una transacción solo puede referenciar billeteras persistidas");
        }
        return billetera.getId();
    }
}
