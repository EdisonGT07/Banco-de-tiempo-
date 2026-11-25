package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "persona")
public class PersonaJpaEntity {
    @Id
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "doc_hmac", nullable = false, length = 64)
    private String docHmac;

    @Column(name = "fecha_nac", nullable = false)
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "zona", length = 10)
    private Persona.Zona zona;

    @Column(name = "estrato")
    private Short estrato;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 12)
    private Persona.Estado estado;

    @Column(name = "bloqueado_hasta")
    private OffsetDateTime bloqueadoHasta;

    @Column(name = "rango_ens_id")
    private Long rangoEnsenanzaId;

    @Column(name = "rango_apr_id")
    private Long rangoAprendizajeId;

    protected PersonaJpaEntity() {
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDocHmac() {
        return docHmac;
    }

    public void setDocHmac(String docHmac) {
        this.docHmac = docHmac;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Persona.Zona getZona() {
        return zona;
    }

    public void setZona(Persona.Zona zona) {
        this.zona = zona;
    }

    public Short getEstrato() {
        return estrato;
    }

    public void setEstrato(Short estrato) {
        this.estrato = estrato;
    }

    public Persona.Estado getEstado() {
        return estado;
    }

    public void setEstado(Persona.Estado estado) {
        this.estado = estado;
    }

    public OffsetDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public void setBloqueadoHasta(OffsetDateTime bloqueadoHasta) {
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public Long getRangoEnsenanzaId() {
        return rangoEnsenanzaId;
    }

    public void setRangoEnsenanzaId(Long rangoEnsenanzaId) {
        this.rangoEnsenanzaId = rangoEnsenanzaId;
    }

    public Long getRangoAprendizajeId() {
        return rangoAprendizajeId;
    }

    public void setRangoAprendizajeId(Long rangoAprendizajeId) {
        this.rangoAprendizajeId = rangoAprendizajeId;
    }
}
