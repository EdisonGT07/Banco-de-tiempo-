package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.security;

import com.bancotiempo.pasaportesaberes.application.port.out.GeneradorCodigoVerificacion;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public final class CodigoVerificacionSeguro implements GeneradorCodigoVerificacion {
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generar() {
        byte[] codigo = new byte[20];
        secureRandom.nextBytes(codigo);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codigo);
    }
}
