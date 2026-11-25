package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.domain.model.Transaccion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Immutable
@Table(name = "transaccion")
public class TransaccionJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 14)
    private Transaccion.TipoTransaccion tipo;

    @Column(name = "horas", nullable = false, precision = 8, scale = 2)
    private BigDecimal horas;

    @Column(name = "billetera_origen_id", nullable = false)
    private Long billeteraOrigenId;

    @Column(name = "billetera_destino_id", nullable = false)
    private Long billeteraDestinoId;

    @Column(name = "sesion_id")
    private Long sesionId;

    @Column(name = "clave_idemp", nullable = false, length = 80)
    private String claveIdempotencia;

    @Column(name = "creada_en", nullable = false)
    private OffsetDateTime creadaEn;

    protected TransaccionJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public Transaccion.TipoTransaccion getTipo() {
        return tipo;
    }

    public void setTipo(Transaccion.TipoTransaccion tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getHoras() {
        return horas;
    }

    public void setHoras(BigDecimal horas) {
        this.horas = horas;
    }

    public Long getBilleteraOrigenId() {
        return billeteraOrigenId;
    }

    public void setBilleteraOrigenId(Long billeteraOrigenId) {
        this.billeteraOrigenId = billeteraOrigenId;
    }

    public Long getBilleteraDestinoId() {
        return billeteraDestinoId;
    }

    public void setBilleteraDestinoId(Long billeteraDestinoId) {
        this.billeteraDestinoId = billeteraDestinoId;
    }

    public Long getSesionId() {
        return sesionId;
    }

    public void setSesionId(Long sesionId) {
        this.sesionId = sesionId;
    }

    public String getClaveIdempotencia() {
        return claveIdempotencia;
    }

    public void setClaveIdempotencia(String claveIdempotencia) {
        this.claveIdempotencia = claveIdempotencia;
    }

    public OffsetDateTime getCreadaEn() {
        return creadaEn;
    }

    public void setCreadaEn(OffsetDateTime creadaEn) {
        this.creadaEn = creadaEn;
    }
}
