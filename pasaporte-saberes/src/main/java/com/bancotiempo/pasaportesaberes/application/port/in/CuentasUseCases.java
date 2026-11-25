package com.bancotiempo.pasaportesaberes.application.port.in;

import com.bancotiempo.pasaportesaberes.domain.model.Persona;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public interface CuentasUseCases {
    Perfil registrarUsuario(RegistroUsuario solicitud);

    SesionAutenticada iniciarSesion(CredencialesInicioSesion solicitud);

    Perfil obtenerMiPerfil(Long usuarioId);

    record RegistroUsuario(
            String nombre,
            String correo,
            String contrasena,
            String documento,
            LocalDate fechaNacimiento,
            Persona.Zona zona,
            Short estrato,
            OffsetDateTime aceptaHabeasDataEn
    ) {
    }

    record CredencialesInicioSesion(String correo, String contrasena) {
    }

    enum TipoCuenta {
        PERSONA,
        ENTIDAD,
        ADMIN
    }

    record Perfil(
            Long id,
            String nombre,
            String correo,
            LocalDate fechaNacimiento,
            Persona.Zona zona,
            Short estrato,
            Persona.Estado estado,
            TipoCuenta tipoCuenta,
            String tipoEntidad,
            String codigoInstitucional,
            Short nivelAcceso
    ) {
        public Perfil(
                Long id,
                String nombre,
                String correo,
                LocalDate fechaNacimiento,
                Persona.Zona zona,
                Short estrato,
                Persona.Estado estado
        ) {
            this(
                    id,
                    nombre,
                    correo,
                    fechaNacimiento,
                    zona,
                    estrato,
                    estado,
                    TipoCuenta.PERSONA,
                    null,
                    null,
                    null
            );
        }
    }

    record SesionAutenticada(String token, Perfil perfil) {
    }
}
