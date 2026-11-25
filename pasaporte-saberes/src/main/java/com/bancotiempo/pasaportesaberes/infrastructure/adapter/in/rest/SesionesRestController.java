package com.bancotiempo.pasaportesaberes.infrastructure.adapter.in.rest;

import com.bancotiempo.pasaportesaberes.application.port.in.SesionesUseCases;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/sesiones")
@SecurityRequirement(name = "bearerAuth")
public class SesionesRestController {
    private final SesionesUseCases sesiones;

    public SesionesRestController(SesionesUseCases sesiones) {
        this.sesiones = sesiones;
    }

    @PostMapping("/{sesionId}/inscripcion")
    @ResponseStatus(HttpStatus.CREATED)
    public RespuestasApi.SesionResponse inscribirse(
            @PathVariable Long sesionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return RespuestasApi.sesion(
                sesiones.inscribirseEnSesion(
                        IdentidadAutenticada.usuarioId(jwt),
                        sesionId
                )
        );
    }

    @DeleteMapping("/{sesionId}/inscripcion")
    public RespuestasApi.SesionResponse cancelarInscripcion(
            @PathVariable Long sesionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return RespuestasApi.sesion(
                sesiones.cancelarInscripcion(
                        IdentidadAutenticada.usuarioId(jwt),
                        sesionId
                )
        );
    }

    @PostMapping("/{sesionId}/handshake")
    @ResponseStatus(HttpStatus.CREATED)
    public RespuestasApi.HandshakeResponse iniciarHandshake(
            @PathVariable Long sesionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return RespuestasApi.handshake(
                sesiones.iniciarHandshake(
                        IdentidadAutenticada.usuarioId(jwt),
                        sesionId
                )
        );
    }

    @PostMapping(path = "/{sesionId}/confirmacion-handshake",
            consumes = "multipart/form-data")
    public RespuestasApi.SesionResponse confirmarHandshake(
            @PathVariable Long sesionId,
            @Valid @RequestPart("solicitud") ConfirmarHandshakeRequest request,
            @RequestPart(value = "foto", required = false) MultipartFile foto,
            @AuthenticationPrincipal Jwt jwt
    ) throws IOException {
        byte[] bytes = foto == null ? null : foto.getBytes();
        String tipoContenido = foto == null ? null : foto.getContentType();
        return RespuestasApi.sesion(sesiones.confirmarHandshake(
                new SesionesUseCases.ConfirmarHandshake(
                        IdentidadAutenticada.usuarioId(jwt),
                        sesionId,
                        request.token(),
                        request.codigoCorto(),
                        bytes,
                        tipoContenido,
                        request.latitud(),
                        request.longitud(),
                        request.consienteEvidencia()
                )
        ));
    }

    @PostMapping("/{sesionId}/confirmacion-fin")
    public RespuestasApi.SesionResponse confirmarFin(
            @PathVariable Long sesionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return RespuestasApi.sesion(sesiones.confirmarFinSesion(
                IdentidadAutenticada.usuarioId(jwt),
                sesionId
        ));
    }

    @PostMapping("/{sesionId}/reportes")
    @ResponseStatus(HttpStatus.CREATED)
    public RespuestasApi.ReporteResponse reportarProblema(
            @PathVariable Long sesionId,
            @Valid @RequestBody ReportarProblemaRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return RespuestasApi.reporte(sesiones.reportarProblema(
                new SesionesUseCases.ReportarProblema(
                        IdentidadAutenticada.usuarioId(jwt),
                        sesionId,
                        request.motivo()
                )
        ));
    }

    public record ConfirmarHandshakeRequest(
            String token,
            String codigoCorto,
            BigDecimal latitud,
            BigDecimal longitud,
            boolean consienteEvidencia
    ) {
    }

    public record ReportarProblemaRequest(@NotBlank String motivo) {
    }
}
