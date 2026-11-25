package com.bancotiempo.pasaportesaberes.infrastructure.adapter.in.rest;

import com.bancotiempo.pasaportesaberes.application.port.in.AdministracionUseCases;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin")
@SecurityRequirement(name = "bearerAuth")
public class AdministracionRestController {
    private final AdministracionUseCases administracion;

    public AdministracionRestController(AdministracionUseCases administracion) {
        this.administracion = administracion;
    }

    @GetMapping("/impacto")
    public AdministracionUseCases.PanelImpacto obtenerPanelImpacto() {
        return administracion.obtenerPanelImpacto();
    }

    @PatchMapping("/reportes/{reporteId}")
    public RespuestasApi.ReporteResponse revisarReporte(
            @PathVariable Long reporteId,
            @Valid @RequestBody RevisionReporteRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return RespuestasApi.reporte(administracion.revisarReporte(
                new AdministracionUseCases.RevisionReporte(
                        IdentidadAutenticada.usuarioId(jwt),
                        reporteId,
                        request.resolver()
                )
        ));
    }

    @PostMapping("/personas/{personaId}/desbloqueo")
    @ResponseStatus(HttpStatus.OK)
    public RespuestasApi.PersonaAdministradaResponse desbloquear(
            @PathVariable Long personaId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return RespuestasApi.persona(administracion.desbloquearUsuario(
                new AdministracionUseCases.DesbloquearUsuario(
                        IdentidadAutenticada.usuarioId(jwt),
                        personaId
                )
        ));
    }

    public record RevisionReporteRequest(@NotNull Boolean resolver) {
    }
}
