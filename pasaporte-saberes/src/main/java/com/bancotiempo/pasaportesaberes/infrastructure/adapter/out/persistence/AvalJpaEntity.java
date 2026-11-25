package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.domain.model.Aval;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "aval")
public class AvalJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entidad_id", nullable = false)
    private Long entidadId;

    @Column(name = "credencial_id", nullable = false)
    private Long credencialId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "vigencia_hasta", nullable = false)
    private LocalDate vigenciaHasta;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 10)
    private Aval.EstadoAval estado;

    protected AvalJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public Long getEntidadId() {
        return entidadId;
    }

    public void setEntidadId(Long entidadId) {
        this.entidadId = entidadId;
    }

    public Long getCredencialId() {
        return credencialId;
    }

    public void setCredencialId(Long credencialId) {
        this.credencialId = credencialId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalDate getVigenciaHasta() {
        return vigenciaHasta;
    }

    public void setVigenciaHasta(LocalDate vigenciaHasta) {
        this.vigenciaHasta = vigenciaHasta;
    }

    public Aval.EstadoAval getEstado() {
        return estado;
    }

    public void setEstado(Aval.EstadoAval estado) {
        this.estado = estado;
    }
}
