package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.storage;

import com.bancotiempo.pasaportesaberes.application.port.out.AlmacenEvidencias;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.Locale;
import java.util.UUID;

@Component
public class SupabaseStorageAlmacenEvidencias implements AlmacenEvidencias {
    private final RestClient restClient;
    private final String supabaseUrl;
    private final String bucket;
    private final String serviceRoleKey;
    private final int maximoBytes;

    public SupabaseStorageAlmacenEvidencias(
            @Value("${supabase.storage.url:}") String supabaseUrl,
            @Value("${supabase.storage.bucket:evidencias}") String bucket,
            @Value("${supabase.storage.service-role-key:}") String serviceRoleKey,
            @Value("${pasaporte-saberes.evidencias.maximo-bytes:5242880}") int maximoBytes
    ) {
        this.restClient = RestClient.create();
        this.supabaseUrl = supabaseUrl;
        this.bucket = bucket;
        this.serviceRoleKey = serviceRoleKey;
        if (maximoBytes <= 0) {
            throw new IllegalArgumentException("El tamaño máximo de evidencia debe ser positivo");
        }
        this.maximoBytes = maximoBytes;
    }

    @Override
    public String guardar(Long sesionId, byte[] contenido, String tipoContenido) {
        if (sesionId == null || sesionId <= 0) {
            throw new IllegalArgumentException("El id de sesión debe ser positivo");
        }
        if (contenido == null || contenido.length == 0 || contenido.length > maximoBytes) {
            throw new IllegalArgumentException(
                    "La foto debe tener contenido y no superar el tamaño permitido");
        }
        validarConfiguracion();
        MediaType tipo = validarTipoYContenido(tipoContenido, contenido);
        String extension = extensionPara(tipo);
        String rutaObjeto = sesionId + "/" + UUID.randomUUID() + extension;
        URI destino = URI.create(
                supabaseUrl.replaceAll("/+$", "")
                        + "/storage/v1/object/" + bucket + "/" + rutaObjeto
        );

        restClient.post()
                .uri(destino)
                .headers(cabeceras -> {
                    cabeceras.setBearerAuth(serviceRoleKey);
                    cabeceras.set("apikey", serviceRoleKey);
                    cabeceras.set("x-upsert", "false");
                })
                .contentType(tipo)
                .body(contenido)
                .retrieve()
                .toBodilessEntity();
        return rutaObjeto;
    }

    private void validarConfiguracion() {
        if (supabaseUrl == null || supabaseUrl.isBlank()) {
            throw new IllegalStateException("Falta configurar SUPABASE_URL");
        }
        if (bucket == null || !bucket.matches("[a-z0-9][a-z0-9._-]{0,62}")) {
            throw new IllegalStateException("SUPABASE_STORAGE_BUCKET no es válido");
        }
        if (serviceRoleKey == null || serviceRoleKey.isBlank()) {
            throw new IllegalStateException("Falta configurar SUPABASE_SERVICE_ROLE_KEY");
        }
        URI base = URI.create(supabaseUrl);
        if (!"https".equalsIgnoreCase(base.getScheme())
                && !("http".equalsIgnoreCase(base.getScheme())
                && ("localhost".equalsIgnoreCase(base.getHost())
                || "127.0.0.1".equals(base.getHost())))) {
            throw new IllegalStateException(
                    "SUPABASE_URL debe usar HTTPS (HTTP solo se permite para localhost)");
        }
    }

    private static MediaType validarTipoYContenido(String tipoContenido, byte[] contenido) {
        if (tipoContenido == null || tipoContenido.isBlank()) {
            throw new IllegalArgumentException("El tipo de contenido de la foto es obligatorio");
        }
        MediaType tipo = MediaType.parseMediaType(tipoContenido);
        String subtipo = tipo.getSubtype().toLowerCase(Locale.ROOT);
        boolean jpeg = "image".equalsIgnoreCase(tipo.getType())
                && "jpeg".equals(subtipo)
                && contenido.length >= 3
                && (contenido[0] & 0xFF) == 0xFF
                && (contenido[1] & 0xFF) == 0xD8
                && (contenido[2] & 0xFF) == 0xFF;
        boolean png = "image".equalsIgnoreCase(tipo.getType())
                && "png".equals(subtipo)
                && contenido.length >= 8
                && (contenido[0] & 0xFF) == 0x89
                && contenido[1] == 'P'
                && contenido[2] == 'N'
                && contenido[3] == 'G'
                && (contenido[4] & 0xFF) == 0x0D
                && (contenido[5] & 0xFF) == 0x0A
                && (contenido[6] & 0xFF) == 0x1A
                && (contenido[7] & 0xFF) == 0x0A;
        boolean webp = "image".equalsIgnoreCase(tipo.getType())
                && "webp".equals(subtipo)
                && contenido.length >= 12
                && coincide(contenido, 0, "RIFF")
                && coincide(contenido, 8, "WEBP");
        if (!jpeg && !png && !webp) {
            throw new IllegalArgumentException(
                    "La evidencia solo admite fotos JPEG, PNG o WebP con firma válida");
        }
        return MediaType.parseMediaType("image/" + subtipo);
    }

    private static boolean coincide(byte[] bytes, int inicio, String firma) {
        if (bytes.length < inicio + firma.length()) {
            return false;
        }
        for (int indice = 0; indice < firma.length(); indice++) {
            if (bytes[inicio + indice] != firma.charAt(indice)) {
                return false;
            }
        }
        return true;
    }

    private static String extensionPara(MediaType tipo) {
        return switch (tipo.getSubtype()) {
            case "jpeg" -> ".jpg";
            case "png" -> ".png";
            case "webp" -> ".webp";
            default -> throw new IllegalArgumentException("Tipo de foto no compatible");
        };
    }
}
