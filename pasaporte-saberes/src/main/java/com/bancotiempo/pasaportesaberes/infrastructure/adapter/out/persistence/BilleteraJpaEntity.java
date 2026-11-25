package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.domain.model.Billetera;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;

@Entity
@Table(name = "billetera")
public class BilleteraJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 10)
    private Billetera.TipoBilletera tipo;

    @Column(name = "persona_id", unique = true)
    private Long personaId;

    @Column(name = "saldo_disponible", nullable = false, precision = 8, scale = 2)
    private BigDecimal saldoDisponible;

    @Column(name = "saldo_reservado", nullable = false, precision = 8, scale = 2)
    private BigDecimal saldoReservado;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected BilleteraJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public Billetera.TipoBilletera getTipo() {
        return tipo;
    }

    public void setTipo(Billetera.TipoBilletera tipo) {
        this.tipo = tipo;
    }

    public Long getPersonaId() {
        return personaId;
    }

    public void setPersonaId(Long personaId) {
        this.personaId = personaId;
    }

    public BigDecimal getSaldoDisponible() {
        return saldoDisponible;
    }

    public void setSaldoDisponible(BigDecimal saldoDisponible) {
        this.saldoDisponible = saldoDisponible;
    }

    public BigDecimal getSaldoReservado() {
        return saldoReservado;
    }

    public void setSaldoReservado(BigDecimal saldoReservado) {
        this.saldoReservado = saldoReservado;
    }

    public Long getVersion() {
        return version;
    }
}
