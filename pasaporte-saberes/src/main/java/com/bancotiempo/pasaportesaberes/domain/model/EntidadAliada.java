package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.UsuarioInvalidoException;

import java.time.OffsetDateTime;

public class EntidadAliada extends Usuario {
    private String tipoEntidad;
    private String codigoInstitucional;

    public EntidadAliada() {
    }

    public EntidadAliada(
            Long id,
            String nombre,
            String correo,
            String contrasenaHash,
            OffsetDateTime habeasDataEn,
            String tipoEntidad,
            String codigoInstitucional
    ) {
        super(id, nombre, correo, contrasenaHash, habeasDataEn);
        setTipoEntidad(tipoEntidad);
        setCodigoInstitucional(codigoInstitucional);
    }

    public String getTipoEntidad() {
        return tipoEntidad;
    }

    public void setTipoEntidad(String tipoEntidad) {
        if (tipoEntidad == null || tipoEntidad.isBlank() || tipoEntidad.length() > 40) {
            throw new UsuarioInvalidoException(
                    "El tipo de entidad es obligatorio y no puede superar 40 caracteres");
        }
        this.tipoEntidad = tipoEntidad;
    }

    public String getCodigoInstitucional() {
        return codigoInstitucional;
    }

    public void setCodigoInstitucional(String codigoInstitucional) {
        if (codigoInstitucional == null || codigoInstitucional.isBlank()
                || codigoInstitucional.length() > 40) {
            throw new UsuarioInvalidoException(
                    "El código institucional es obligatorio y no puede superar 40 caracteres");
        }
        this.codigoInstitucional = codigoInstitucional;
    }

    @Override
    public void validarDatosObligatorios() {
        super.validarDatosObligatorios();
        if (tipoEntidad == null || tipoEntidad.isBlank() || tipoEntidad.length() > 40) {
            throw new UsuarioInvalidoException(
                    "El tipo de entidad es obligatorio y no puede superar 40 caracteres");
        }
        if (codigoInstitucional == null || codigoInstitucional.isBlank()
                || codigoInstitucional.length() > 40) {
            throw new UsuarioInvalidoException(
                    "El código institucional es obligatorio y no puede superar 40 caracteres");
        }
    }
}
