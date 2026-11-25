package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.security;

import com.bancotiempo.pasaportesaberes.application.port.out.FirmadorCredenciales;
import com.bancotiempo.pasaportesaberes.application.port.out.VerificadorCredenciales;
import com.bancotiempo.pasaportesaberes.domain.model.Credencial;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

@Component
public final class HmacFirmaCredenciales
        implements FirmadorCredenciales, VerificadorCredenciales {
    private static final String ALGORITMO = "HmacSHA256";
    private final SecretKeySpec clave;

    public HmacFirmaCredenciales(
            @Value("${security.credential-signature.secret}") String secreto
    ) {
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException(
                    "FIRMA_CLAVE debe contener al menos 256 bits");
        }
        this.clave = new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), ALGORITMO);
    }

    @Override
    public String firmar(
            Long personaId,
            Long habilidadId,
            BigDecimal horasEnsenadas,
            BigDecimal ratingPromedio,
            String codigoVerificacion,
            OffsetDateTime actualizadaEn
    ) {
        String contenido = contenidoCanonico(
                personaId,
                habilidadId,
                horasEnsenadas,
                ratingPromedio,
                codigoVerificacion,
                actualizadaEn
        );
        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(clave);
            return HexFormat.of().formatHex(mac.doFinal(
                    contenido.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("No se pudo firmar la credencial", exception);
        }
    }

    @Override
    public boolean verificar(Credencial credencial) {
        if (credencial == null || credencial.firma() == null
                || !credencial.firma().matches("[0-9a-fA-F]{64}")) {
            return false;
        }
        String firmaEsperada = firmar(
                credencial.persona().getId(),
                credencial.habilidad().id(),
                credencial.horasEnsenadas(),
                credencial.ratingPromedio(),
                credencial.codigoVerificacion(),
                credencial.actualizadaEn()
        );
        return MessageDigest.isEqual(
                HexFormat.of().parseHex(firmaEsperada),
                HexFormat.of().parseHex(credencial.firma())
        );
    }

    private static String contenidoCanonico(
            Long personaId,
            Long habilidadId,
            BigDecimal horasEnsenadas,
            BigDecimal ratingPromedio,
            String codigoVerificacion,
            OffsetDateTime actualizadaEn
    ) {
        if (personaId == null || habilidadId == null || horasEnsenadas == null
                || codigoVerificacion == null || actualizadaEn == null) {
            throw new IllegalArgumentException(
                    "Los datos necesarios para firmar la credencial son obligatorios");
        }
        return String.join("\n",
                personaId.toString(),
                habilidadId.toString(),
                horasEnsenadas.toPlainString(),
                ratingPromedio == null ? "" : ratingPromedio.toPlainString(),
                codigoVerificacion,
                actualizadaEn.toInstant().truncatedTo(ChronoUnit.MICROS).toString()
        );
    }
}
