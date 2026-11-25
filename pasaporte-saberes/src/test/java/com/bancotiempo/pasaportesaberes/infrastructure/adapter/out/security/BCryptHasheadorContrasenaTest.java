package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BCryptHasheadorContrasenaTest {
    private final BCryptHasheadorContrasena hasheador =
            new BCryptHasheadorContrasena(new BCryptPasswordEncoder());

    @Test
    void hasheaLaContrasenaYPermiteVerificarlaSinAlmacenarlaEnClaro() {
        String hash = hasheador.hashear("una-clave-segura");

        assertThat(hash)
                .startsWith("$2")
                .isNotEqualTo("una-clave-segura");
        assertThat(hasheador.coincide("una-clave-segura", hash)).isTrue();
        assertThat(hasheador.coincide("otra-clave", hash)).isFalse();
    }

    @Test
    void rechazaContrasenasVaciasAlCrearElHash() {
        assertThatThrownBy(() -> hasheador.hashear(" "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(hasheador.coincide("clave", null)).isFalse();
    }
}
