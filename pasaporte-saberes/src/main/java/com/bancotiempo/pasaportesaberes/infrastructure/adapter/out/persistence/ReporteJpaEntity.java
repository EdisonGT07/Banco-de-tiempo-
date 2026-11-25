package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.domain.model.Reporte;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "reporte")
public class ReporteJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sesion_id", nullable = false)
    private Long sesionId;

    @Column(name = "autor_id", nullable = false)
    private Long autorId;

    @Column(name = "revisor_id")
    private Long revisorId;

    @Column(name = "motivo", nullable = false)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 12)
    private Reporte.EstadoReporte estado;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    protected ReporteJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public Long getSesionId() {
        return sesionId;
    }

    public void setSesionId(Long sesionId) {
        this.sesionId = sesionId;
    }

    public Long getAutorId() {
        return autorId;
    }

    public void setAutorId(Long autorId) {
        this.autorId = autorId;
    }

    public Long getRevisorId() {
        return revisorId;
    }

    public void setRevisorId(Long revisorId) {
        this.revisorId = revisorId;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Reporte.EstadoReporte getEstado() {
        return estado;
    }

    public void setEstado(Reporte.EstadoReporte estado) {
        this.estado = estado;
    }

    public OffsetDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(OffsetDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}
