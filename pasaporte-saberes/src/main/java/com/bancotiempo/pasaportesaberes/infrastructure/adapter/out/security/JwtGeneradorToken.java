package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.security;

import com.bancotiempo.pasaportesaberes.application.port.out.GeneradorToken;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.domain.model.Administrador;
import com.bancotiempo.pasaportesaberes.domain.model.EntidadAliada;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import com.bancotiempo.pasaportesaberes.domain.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
public final class JwtGeneradorToken implements GeneradorToken {
    public static final String ISSUER = "https://pasaporte-saberes.local";

    private final RepositorioUsuario usuarios;
    private final JwtEncoder jwtEncoder;
    private final Clock clock;
    private final Duration vigenciaTokenAcceso;
    private final SecureRandom secureRandom = new SecureRandom();

    public JwtGeneradorToken(
            RepositorioUsuario usuarios,
            JwtEncoder jwtEncoder,
            Clock clock,
            @Value("${security.jwt.access-token-ttl}") Duration vigenciaTokenAcceso
    ) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.jwtEncoder = Objects.requireNonNull(jwtEncoder);
        this.clock = Objects.requireNonNull(clock);
        if (vigenciaTokenAcceso == null || vigenciaTokenAcceso.isNegative()
                || vigenciaTokenAcceso.isZero()) {
            throw new IllegalArgumentException("La vigencia del JWT debe ser positiva");
        }
        this.vigenciaTokenAcceso = vigenciaTokenAcceso;
    }

    @Override
    public String generarTokenSesion() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    @Override
    public String generarCodigoCorto() {
        return String.format(Locale.ROOT, "%06d", secureRandom.nextInt(1_000_000));
    }

    @Override
    public String generarTokenAcceso(Long usuarioId) {
        if (usuarioId == null || usuarioId <= 0) {
            throw new IllegalArgumentException("El id del usuario debe ser positivo");
        }
        Usuario usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontró el usuario para generar el token"));
        Instant emitidoEn = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(usuarioId.toString())
                .issuedAt(emitidoEn)
                .expiresAt(emitidoEn.plus(vigenciaTokenAcceso))
                .claim("roles", List.of(rolDe(usuario)))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
                .type("JWT")
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private static String rolDe(Usuario usuario) {
        if (usuario instanceof Administrador) {
            return "ROLE_ADMIN";
        }
        if (usuario instanceof EntidadAliada) {
            return "ROLE_ENTIDAD";
        }
        if (usuario instanceof Persona) {
            return "ROLE_PERSONA";
        }
        throw new IllegalStateException("Tipo de usuario no soportado para JWT");
    }
}
