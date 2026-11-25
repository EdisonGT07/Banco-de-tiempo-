package com.bancotiempo.pasaportesaberes.infrastructure.adapter.in.rest;

import org.springframework.security.oauth2.jwt.Jwt;

final class IdentidadAutenticada {
    private IdentidadAutenticada() {
    }

    static Long usuarioId(Jwt jwt) {
        return Long.parseLong(jwt.getSubject());
    }
}
