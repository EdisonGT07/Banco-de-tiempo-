package com.bancotiempo.pasaportesaberes.infrastructure.adapter.in.rest;

import com.bancotiempo.pasaportesaberes.application.port.in.PasaporteUseCases;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pasaportes")
public class PasaporteRestController {
    private final PasaporteUseCases pasaporte;

    public PasaporteRestController(PasaporteUseCases pasaporte) {
        this.pasaporte = pasaporte;
    }

    @GetMapping("/personas/{personaId}")
    @SecurityRequirement(name = "bearerAuth")
    public RespuestasApi.PasaporteResponse obtenerPasaporte(
            @PathVariable Long personaId
    ) {
        return RespuestasApi.pasaporte(pasaporte.obtenerPasaporte(personaId));
    }

    @PostMapping("/personas/me/rangos/recalcular")
    @SecurityRequirement(name = "bearerAuth")
    public RespuestasApi.RangosResponse recalcularMisRangos(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return RespuestasApi.rangos(
                pasaporte.calcularRangos(IdentidadAutenticada.usuarioId(jwt)));
    }

    @GetMapping("/verificar/{codigo}")
    public RespuestasApi.VerificacionResponse verificarCredencial(
            @PathVariable String codigo
    ) {
        return RespuestasApi.verificacion(pasaporte.verificarCredencial(codigo));
    }

    @GetMapping("/credenciales/{credencialId}/certificado.pdf")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<byte[]> emitirCertificado(@PathVariable Long credencialId) {
        byte[] pdf = pasaporte.emitirCertificadoPdf(credencialId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("certificado-" + credencialId + ".pdf")
                .build());
        headers.setContentLength(pdf.length);
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
