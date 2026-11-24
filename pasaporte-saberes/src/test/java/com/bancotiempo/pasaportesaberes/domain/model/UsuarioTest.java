package com.bancotiempo.pasaportesaberes.domain.model;

import com.bancotiempo.pasaportesaberes.domain.exception.UsuarioInvalidoException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsuarioTest {
    private final OffsetDateTime aceptacion = OffsetDateTime.parse("2026-11-24T10:00:00Z");

    @Test
    void entidadAliadaValidaCamposComunesYDatosInstitucionales() {
        EntidadAliada entidad = new EntidadAliada(
                1L, "Institución", "contacto@example.com", "$2a$hash", aceptacion,
                "Educativa", "COD-001"
        );

        assertThat(entidad.getNombre()).isEqualTo("Institución");
        assertThat(entidad.getCodigoInstitucional()).isEqualTo("COD-001");
        assertThatThrownBy(() -> new EntidadAliada(
                1L, " ", "contacto@example.com", "$2a$hash", aceptacion,
                "Educativa", "COD-001"
        )).isInstanceOf(UsuarioInvalidoException.class);
        assertThatThrownBy(() -> new EntidadAliada(
                1L, "Institución", "contacto@example.com", "$2a$hash", aceptacion,
                "Educativa", " "
        )).isInstanceOf(UsuarioInvalidoException.class);
    }

    @Test
    void administradorExigeNivelDeAcceso() {
        Administrador administrador = new Administrador(
                2L, "Admin", "admin@example.com", "$2a$hash", aceptacion, (short) 1
        );

        assertThat(administrador.getNivelAcceso()).isEqualTo((short) 1);
        assertThatThrownBy(() -> administrador.setNivelAcceso(null))
                .isInstanceOf(UsuarioInvalidoException.class);
    }

    @Test
    void usuarioValidaLongitudYCamposObligatorios() {
        EntidadAliada entidad = new EntidadAliada(
                1L, "Institución", "contacto@example.com", "$2a$hash", aceptacion,
                "Educativa", "COD-001"
        );

        assertThatThrownBy(() -> entidad.setCorreo("x".repeat(161)))
                .isInstanceOf(UsuarioInvalidoException.class);
        assertThatThrownBy(() -> entidad.setHabeasDataEn(null))
                .isInstanceOf(UsuarioInvalidoException.class);
        assertThatCode(entidad::validarDatosObligatorios).doesNotThrowAnyException();
    }
}
