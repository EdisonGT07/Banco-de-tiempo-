package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "evidencia")
public class EvidenciaJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sesion_id", nullable = false, unique = true)
    private Long sesionId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "codigo_corto_hash", nullable = false, length = 64)
    private String codigoCortoHash;

    @Column(name = "expira_en", nullable = false)
    private OffsetDateTime expiraEn;

    @Column(name = "usado_en")
    private OffsetDateTime usadoEn;

    @Column(name = "intentos_fallidos", nullable = false)
    private Short intentosFallidos;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "latitud", precision = 9, scale = 6)
    private BigDecimal latitud;

    @Column(name = "longitud", precision = 9, scale = 6)
    private BigDecimal longitud;

    @Column(name = "marca_servidor", nullable = false)
    private OffsetDateTime marcaServidor;

    protected EvidenciaJpaEntity() {
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

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public String getCodigoCortoHash() {
        return codigoCortoHash;
    }

    public void setCodigoCortoHash(String codigoCortoHash) {
        this.codigoCortoHash = codigoCortoHash;
    }

    public OffsetDateTime getExpiraEn() {
        return expiraEn;
    }

    public void setExpiraEn(OffsetDateTime expiraEn) {
        this.expiraEn = expiraEn;
    }

    public OffsetDateTime getUsadoEn() {
        return usadoEn;
    }

    public void setUsadoEn(OffsetDateTime usadoEn) {
        this.usadoEn = usadoEn;
    }

    public Short getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(Short intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public BigDecimal getLatitud() {
        return latitud;
    }

    public void setLatitud(BigDecimal latitud) {
        this.latitud = latitud;
    }

    public BigDecimal getLongitud() {
        return longitud;
    }

    public void setLongitud(BigDecimal longitud) {
        this.longitud = longitud;
    }

    public OffsetDateTime getMarcaServidor() {
        return marcaServidor;
    }

    public void setMarcaServidor(OffsetDateTime marcaServidor) {
        this.marcaServidor = marcaServidor;
    }
}
