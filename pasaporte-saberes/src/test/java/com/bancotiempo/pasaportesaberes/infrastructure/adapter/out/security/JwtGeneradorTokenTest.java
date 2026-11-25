package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.security;

import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.domain.model.Administrador;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtGeneradorTokenTest {
    @Test
    void tokenAccesoIncluyeSujetoRolEmisionYExpiracion() {
        RepositorioUsuario usuarios = mock(RepositorioUsuario.class);
        JwtEncoder encoder = mock(JwtEncoder.class);
        Persona persona = new Persona();
        persona.setId(42L);
        when(usuarios.buscarPorId(42L)).thenReturn(Optional.of(persona));
        when(encoder.encode(org.mockito.ArgumentMatchers.any(JwtEncoderParameters.class)))
                .thenReturn(Jwt.withTokenValue("jwt-firmado")
                        .header("alg", "HS256")
                        .claim("sub", "42")
                        .build());
        Instant ahora = Instant.parse("2026-11-24T10:00:00Z");
        JwtGeneradorToken generador = new JwtGeneradorToken(
                usuarios,
                encoder,
                Clock.fixed(ahora, ZoneOffset.UTC),
                Duration.ofMinutes(15)
        );

        assertThat(generador.generarTokenAcceso(42L)).isEqualTo("jwt-firmado");
        verify(encoder).encode(argThat(parametros -> {
            var claims = parametros.getClaims();
            return claims.getSubject().equals("42")
                    && claims.getIssuer().toString().equals(JwtGeneradorToken.ISSUER)
                    && claims.getIssuedAt().equals(ahora)
                    && claims.getExpiresAt().equals(ahora.plus(Duration.ofMinutes(15)))
                    && claims.getClaim("roles").equals(List.of("ROLE_PERSONA"));
        }));
    }

    @Test
    void asignaElRolDeAdministradorYGeneraCredencialesDeHandshakeConFormatoSeguro() {
        RepositorioUsuario usuarios = mock(RepositorioUsuario.class);
        JwtEncoder encoder = mock(JwtEncoder.class);
        Administrador admin = new Administrador();
        admin.setId(7L);
        when(usuarios.buscarPorId(7L)).thenReturn(Optional.of(admin));
        when(encoder.encode(org.mockito.ArgumentMatchers.any(JwtEncoderParameters.class)))
                .thenReturn(Jwt.withTokenValue("jwt-admin")
                        .header("alg", "HS256")
                        .claim("sub", "7")
                        .build());
        JwtGeneradorToken generador = new JwtGeneradorToken(
                usuarios,
                encoder,
                Clock.systemUTC(),
                Duration.ofMinutes(10)
        );

        generador.generarTokenAcceso(7L);

        verify(encoder).encode(argThat(parametros ->
                parametros.getClaims().getClaim("roles").equals(List.of("ROLE_ADMIN"))));
        assertThat(generador.generarTokenSesion()).matches("[0-9a-f]{32}");
        assertThat(generador.generarCodigoCorto()).matches("[0-9]{6}");
    }

    @Test
    void noGeneraTokensParaIdsInvalidos() {
        JwtGeneradorToken generador = new JwtGeneradorToken(
                mock(RepositorioUsuario.class),
                mock(JwtEncoder.class),
                Clock.systemUTC(),
                Duration.ofMinutes(10)
        );

        assertThatThrownBy(() -> generador.generarTokenAcceso(0L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
