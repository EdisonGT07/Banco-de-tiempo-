package com.bancotiempo.pasaportesaberes.domain.model;

public class Administrador extends Usuario {
    private Short nivelAcceso = 1;

    public Short getNivelAcceso() {
        return nivelAcceso;
    }

    public void setNivelAcceso(Short nivelAcceso) {
        this.nivelAcceso = nivelAcceso;
    }
}
