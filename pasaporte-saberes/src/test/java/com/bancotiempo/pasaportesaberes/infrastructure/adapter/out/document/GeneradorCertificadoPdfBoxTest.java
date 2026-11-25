package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.document;

import com.bancotiempo.pasaportesaberes.application.port.in.PasaporteUseCases;
import com.bancotiempo.pasaportesaberes.domain.model.Credencial;
import org.apache.pdfbox.Loader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GeneradorCertificadoPdfBoxTest {
    private final GeneradorCertificadoPdfBox generador = new GeneradorCertificadoPdfBox();

    @Test
    void generaUnPdfLegibleParaLaCredencial() throws IOException {
        PasaporteUseCases.CredencialPublica credencial =
                new PasaporteUseCases.CredencialPublica(
                        3L,
                        "Guitarra",
                        new BigDecimal("12.00"),
                        new BigDecimal("4.50"),
                        Credencial.Nivel.VALIDADA_POR_PARES,
                        null
                );

        byte[] pdf = generador.generar(credencial, "CODIGO-VERIFICABLE");

        try (var documento = Loader.loadPDF(pdf)) {
            assertEquals(1, documento.getNumberOfPages());
        }
    }
}
