package com.bancotiempo.pasaportesaberes.infrastructure.adapter.in.rest;

import com.bancotiempo.pasaportesaberes.application.port.in.CuentasUseCases;
import com.bancotiempo.pasaportesaberes.infrastructure.config.ConfiguracionSeguridad;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CuentasRestController.class)
@Import({ConfiguracionSeguridad.class, RestExceptionHandler.class})
@TestPropertySource(properties = {
        "security.jwt.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "security.jwt.access-token-ttl=PT15M"
})
class CuentasRestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CuentasUseCases cuentas;

    @Test
    void obtieneElPerfilDelSujetoAutenticado() throws Exception {
        when(cuentas.obtenerMiPerfil(42L)).thenReturn(new CuentasUseCases.Perfil(
                42L,
                "Ada",
                "ada@example.com",
                LocalDate.of(1990, 1, 1),
                null,
                null,
                null
        ));

        mockMvc.perform(get("/api/v1/cuentas/me")
                        .with(jwt().jwt(token -> token.subject("42"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.correo").value("ada@example.com"));

        verify(cuentas).obtenerMiPerfil(42L);
    }

    @Test
    void rechazaUnaSolicitudSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/cuentas/me"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(cuentas);
    }

    @Test
    void rechazaElRegistroSiNoSeAceptaElTratamientoDeDatos() throws Exception {
        String solicitud = """
                {
                  "nombre": "Ada",
                  "correo": "ada@example.com",
                  "contrasena": "secreto",
                  "documento": "123456",
                  "fechaNacimiento": "1990-01-01",
                  "zona": "URBANA",
                  "estrato": 2,
                  "aceptaHabeasData": false
                }
                """;

        mockMvc.perform(post("/api/v1/cuentas/registro")
                        .contentType("application/json")
                        .content(solicitud))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"));

        verifyNoInteractions(cuentas);
    }

}
