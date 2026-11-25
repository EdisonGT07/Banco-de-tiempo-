package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.security;

import com.bancotiempo.pasaportesaberes.application.port.out.GeneradorHmacDocumento;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;

@Component
public final class HmacGeneradorDocumento implements GeneradorHmacDocumento {
    private static final String ALGORITMO = "HmacSHA256";
    private final SecretKeySpec clave;

    public HmacGeneradorDocumento(@Value("${security.document-hmac.secret}") String secreto) {
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException(
                    "DOC_PEPPER debe contener al menos 256 bits");
        }
        this.clave = new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), ALGORITMO);
    }

    @Override
    public String generar(String documento) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio");
        }
        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(clave);
            return HexFormat.of().formatHex(mac.doFinal(
                    documento.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("No se pudo calcular el HMAC del documento", exception);
        }
    }
}
