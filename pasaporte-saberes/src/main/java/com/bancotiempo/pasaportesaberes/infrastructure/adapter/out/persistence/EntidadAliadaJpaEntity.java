package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "entidad_aliada")
public class EntidadAliadaJpaEntity {
    @Id
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo;

    @Column(name = "tipo_entidad", nullable = false, length = 40)
    private String tipoEntidad;

    @Column(name = "codigo_inst", nullable = false, length = 40)
    private String codigoInstitucional;

    protected EntidadAliadaJpaEntity() {
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

    public String getTipoEntidad() {
        return tipoEntidad;
    }

    public void setTipoEntidad(String tipoEntidad) {
        this.tipoEntidad = tipoEntidad;
    }

    public String getCodigoInstitucional() {
        return codigoInstitucional;
    }

    public void setCodigoInstitucional(String codigoInstitucional) {
        this.codigoInstitucional = codigoInstitucional;
    }
}
