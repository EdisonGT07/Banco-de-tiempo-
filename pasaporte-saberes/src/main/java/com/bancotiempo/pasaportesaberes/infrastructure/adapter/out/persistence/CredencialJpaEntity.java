package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "credencial")
public class CredencialJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "persona_id", nullable = false)
    private Long personaId;

    @Column(name = "habilidad_id", nullable = false)
    private Long habilidadId;

    @Column(name = "horas_ensenadas", nullable = false, precision = 8, scale = 2)
    private BigDecimal horasEnsenadas;

    @Column(name = "rating_promedio", precision = 3, scale = 2)
    private BigDecimal ratingPromedio;

    @Column(name = "codigo_verif", nullable = false, length = 32, unique = true)
    private String codigoVerificacion;

    @Column(name = "firma", nullable = false)
    private String firma;

    @Column(name = "actualizada_en", nullable = false)
    private OffsetDateTime actualizadaEn;

    @Column(name = "num_verificaciones", nullable = false)
    private Long numeroVerificaciones;

    protected CredencialJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public Long getPersonaId() {
        return personaId;
    }

    public void setPersonaId(Long personaId) {
        this.personaId = personaId;
    }

    public Long getHabilidadId() {
        return habilidadId;
    }

    public void setHabilidadId(Long habilidadId) {
        this.habilidadId = habilidadId;
    }

    public BigDecimal getHorasEnsenadas() {
        return horasEnsenadas;
    }

    public void setHorasEnsenadas(BigDecimal horasEnsenadas) {
        this.horasEnsenadas = horasEnsenadas;
    }

    public BigDecimal getRatingPromedio() {
        return ratingPromedio;
    }

    public void setRatingPromedio(BigDecimal ratingPromedio) {
        this.ratingPromedio = ratingPromedio;
    }

    public String getCodigoVerificacion() {
        return codigoVerificacion;
    }

    public void setCodigoVerificacion(String codigoVerificacion) {
        this.codigoVerificacion = codigoVerificacion;
    }

    public String getFirma() {
        return firma;
    }

    public void setFirma(String firma) {
        this.firma = firma;
    }

    public OffsetDateTime getActualizadaEn() {
        return actualizadaEn;
    }

    public void setActualizadaEn(OffsetDateTime actualizadaEn) {
        this.actualizadaEn = actualizadaEn;
    }

    public Long getNumeroVerificaciones() {
        return numeroVerificaciones;
    }

    public void setNumeroVerificaciones(Long numeroVerificaciones) {
        this.numeroVerificaciones = numeroVerificaciones;
    }
}
