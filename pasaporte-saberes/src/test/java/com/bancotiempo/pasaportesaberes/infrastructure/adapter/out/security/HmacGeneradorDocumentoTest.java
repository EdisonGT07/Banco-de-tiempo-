package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HmacGeneradorDocumentoTest {
    @Test
    void produceHmacDeterministaYNoReversible() {
        HmacGeneradorDocumento generador =
                new HmacGeneradorDocumento("pepper-de-pruebas-con-mas-de-32-bytes");

        String hmac = generador.generar("documento-123");

        assertThat(hmac).matches("[0-9a-f]{64}");
        assertThat(generador.generar("documento-123")).isEqualTo(hmac);
        assertThat(generador.generar("documento-124")).isNotEqualTo(hmac);
    }

    @Test
    void rechazaPepperInsuficienteYDocumentoVacio() {
        assertThatThrownBy(() -> new HmacGeneradorDocumento("corto"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new HmacGeneradorDocumento(
                "pepper-de-pruebas-con-mas-de-32-bytes").generar(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
