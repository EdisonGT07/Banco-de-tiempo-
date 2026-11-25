package com.bancotiempo.pasaportesaberes.infrastructure.adapter.out.storage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SupabaseStorageAlmacenEvidenciasTest {
    @Test
    void rechazaImagenSinFirmaValidaAntesDeEnviarLaSolicitud() {
        SupabaseStorageAlmacenEvidencias almacen =
                new SupabaseStorageAlmacenEvidencias(
                        "https://example.supabase.co",
                        "evidencias",
                        "clave-no-usada-en-la-prueba",
                        1024
                );

        assertThrows(IllegalArgumentException.class, () -> almacen.guardar(
                17L,
                new byte[]{1, 2, 3},
                "image/jpeg"
        ));
    }
}
