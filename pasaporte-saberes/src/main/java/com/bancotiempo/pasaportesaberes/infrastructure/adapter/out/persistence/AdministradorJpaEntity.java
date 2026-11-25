package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "administrador")
public class AdministradorJpaEntity {
    @Id
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "tipo", nullable = false, length = 10)
    private String tipo;

    @Column(name = "nivel_acceso", nullable = false)
    private Short nivelAcceso;

    protected AdministradorJpaEntity() {
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

    public Short getNivelAcceso() {
        return nivelAcceso;
    }

    public void setNivelAcceso(Short nivelAcceso) {
        this.nivelAcceso = nivelAcceso;
    }
}
