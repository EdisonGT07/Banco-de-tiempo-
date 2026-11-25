package com.bancotiempo.pasaportesaberes.infrastructure.config;

import com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.security.JwtGeneradorToken;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConfiguracionSeguridadTest {
    private static final String CLAVE_TEST_BASE64 =
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

    @Test
    void configuraSecurityEncoderDecoderYHashDeContrasenas() {
        new ApplicationContextRunner()
                .withUserConfiguration(ConfiguracionSeguridad.class)
                .withPropertyValues(
                        "security.jwt.secret-base64=" + CLAVE_TEST_BASE64,
                        "security.jwt.access-token-ttl=PT15M",
                        "security.cors.allowed-origins=http://localhost:5173"
                )
                .run(context -> {
                    assertThat(context.getStartupFailure()).isNull();
                    assertThat(context).hasSingleBean(SecurityFilterChain.class);

                    Instant ahora = Instant.now();
                    JwtClaimsSet claims = JwtClaimsSet.builder()
                            .issuer(JwtGeneradorToken.ISSUER)
                            .subject("42")
                            .issuedAt(ahora)
                            .expiresAt(ahora.plusSeconds(300))
                            .claim("roles", List.of("ROLE_PERSONA"))
                            .build();
                    JwtEncoder encoder = context.getBean(JwtEncoder.class);
                    String token = encoder.encode(JwtEncoderParameters.from(
                            JwsHeader.with(MacAlgorithm.HS256).build(),
                            claims
                    )).getTokenValue();
                    var decodificado = context.getBean(JwtDecoder.class).decode(token);

                    assertThat(decodificado.getSubject()).isEqualTo("42");
                    assertThat(decodificado.getIssuer().toString())
                            .isEqualTo(JwtGeneradorToken.ISSUER);
                    assertThat(context.getBean(PasswordEncoder.class)
                            .matches("clave", context.getBean(PasswordEncoder.class)
                                    .encode("clave"))).isTrue();
                });
    }

    @Test
    void rechazaUnaClaveJwtMasCortaDe256Bits() {
        new ApplicationContextRunner()
                .withUserConfiguration(ConfiguracionSeguridad.class)
                .withPropertyValues(
                        "security.jwt.secret-base64=YWJjZA==",
                        "security.jwt.access-token-ttl=PT15M"
                )
                .run(context -> assertThat(context.getStartupFailure())
                        .hasRootCauseMessage("La clave JWT debe contener al menos 256 bits"));
    }
}
