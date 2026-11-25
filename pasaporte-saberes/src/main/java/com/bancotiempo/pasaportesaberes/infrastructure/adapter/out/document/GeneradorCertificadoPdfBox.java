package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.document;

import com.bancotiempo.pasaportesaberes.application.port.in.PasaporteUseCases;
import com.bancotiempo.pasaportesaberes.application.port.out.GeneradorCertificadoPdf;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class GeneradorCertificadoPdfBox implements GeneradorCertificadoPdf {
    private static final float MARGEN_IZQUIERDO = 54;
    private static final float ESPACIO_LINEA = 24;

    @Override
    public byte[] generar(
            PasaporteUseCases.CredencialPublica credencial,
            String codigoVerificacion
    ) {
        Objects.requireNonNull(credencial, "La credencial es obligatoria");
        if (codigoVerificacion == null || codigoVerificacion.isBlank()) {
            throw new IllegalArgumentException("El código de verificación es obligatorio");
        }

        try (PDDocument documento = new PDDocument();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            PDPage pagina = new PDPage(PDRectangle.LETTER);
            documento.addPage(pagina);
            try (PDPageContentStream contenido =
                         new PDPageContentStream(documento, pagina)) {
                escribirContenido(contenido, credencial, codigoVerificacion);
            }
            documento.save(salida);
            return salida.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo generar el certificado PDF", exception);
        }
    }

    private static void escribirContenido(
            PDPageContentStream contenido,
            PasaporteUseCases.CredencialPublica credencial,
            String codigoVerificacion
    ) throws IOException {
        PDType1Font negrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        contenido.beginText();
        contenido.setFont(negrita, 18);
        contenido.newLineAtOffset(MARGEN_IZQUIERDO, 730);
        contenido.showText("PASAPORTE DE SABERES");
        contenido.newLineAtOffset(0, -38);
        contenido.setFont(normal, 12);

        List<String> lineas = new ArrayList<>(List.of(
                "Constancia de experiencia de ensenanza",
                "Credencial: " + credencial.id(),
                "Habilidad: " + limpiar(credencial.habilidad()),
                "Horas ensenadas: " + credencial.horasEnsenadas().toPlainString(),
                "Calificacion: " + (credencial.rating() == null
                        ? "Sin calificaciones"
                        : credencial.rating().toPlainString()),
                "Nivel: " + credencial.nivel().name(),
                "Codigo de verificacion: " + limpiar(codigoVerificacion)
        ));
        if (credencial.aval() != null) {
            lineas.add("Entidad que avala: " + limpiar(credencial.aval().entidad().getNombre()));
            lineas.add("Vigente hasta: " + credencial.aval().vigenciaHasta());
        }
        lineas.add("Consulte el estado vigente usando el codigo de verificacion.");

        float posicionY = 0;
        for (String linea : lineas) {
            List<String> fragmentos = dividir(linea, 82);
            for (String fragmento : fragmentos) {
                contenido.showText(fragmento);
                contenido.newLineAtOffset(0, -ESPACIO_LINEA);
                posicionY -= ESPACIO_LINEA;
            }
        }
        if (posicionY < -500) {
            throw new IllegalArgumentException("La informacion excede una pagina PDF");
        }
        contenido.endText();
    }

    private static List<String> dividir(String texto, int ancho) {
        String seguro = limpiar(texto);
        if (seguro.length() <= ancho) {
            return List.of(seguro);
        }
        List<String> lineas = new ArrayList<>();
        StringBuilder linea = new StringBuilder();
        for (String palabra : seguro.split("\\s+")) {
            if (linea.length() > 0 && linea.length() + palabra.length() + 1 > ancho) {
                lineas.add(linea.toString());
                linea.setLength(0);
            }
            if (linea.length() > 0) {
                linea.append(' ');
            }
            linea.append(palabra);
        }
        if (linea.length() > 0) {
            lineas.add(linea.toString());
        }
        return lineas;
    }

    private static String limpiar(String texto) {
        return texto == null ? "" : texto.replaceAll("[\\r\\n\\t]+", " ").trim();
    }
}
