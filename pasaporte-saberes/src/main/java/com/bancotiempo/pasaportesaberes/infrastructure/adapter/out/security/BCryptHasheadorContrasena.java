package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.security;

import com.bancotiempo.pasaportesaberes.application.port.out.HasheadorContrasena;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

public final class BCryptHasheadorContrasena implements HasheadorContrasena {
    private final PasswordEncoder passwordEncoder;

    public BCryptHasheadorContrasena(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder);
    }

    @Override
    public String hashear(String contrasena) {
        if (contrasena == null || contrasena.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        return passwordEncoder.encode(contrasena);
    }

    @Override
    public boolean coincide(String contrasena, String hash) {
        if (contrasena == null || hash == null || hash.isBlank()) {
            return false;
        }
        return passwordEncoder.matches(contrasena, hash);
    }
}
