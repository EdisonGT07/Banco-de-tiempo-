package com.bancotiempo.pasaportesaberes.infrastructure.adapter.in.rest;

import com.bancotiempo.pasaportesaberes.application.port.in.InstitucionesUseCases;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class InstitucionesRestController {
    private final InstitucionesUseCases instituciones;

    public InstitucionesRestController(InstitucionesUseCases instituciones) {
        this.instituciones = instituciones;
    }

    @PostMapping("/api/v1/personas/me/credenciales/{credencialId}/solicitud-aval")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @SecurityRequirement(name = "bearerAuth")
    public void solicitarAval(
            @PathVariable Long credencialId,
            @Valid @RequestBody SolicitudAvalRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        instituciones.solicitarAval(new InstitucionesUseCases.SolicitudAval(
                IdentidadAutenticada.usuarioId(jwt),
                credencialId,
                request.entidadId()
        ));
    }

    @PostMapping("/api/v1/instituciones/avales")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public RespuestasApi.AvalResponse registrarAval(
            @Valid @RequestBody RegistrarAvalRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return RespuestasApi.aval(instituciones.registrarAval(
                new InstitucionesUseCases.RegistrarAval(
                        IdentidadAutenticada.usuarioId(jwt),
                        request.entidadId(),
                        request.credencialId(),
                        request.fecha(),
                        request.vigenciaHasta()
                )
        ));
    }

    public record SolicitudAvalRequest(@NotNull Long entidadId) {
    }

    public record RegistrarAvalRequest(
            @NotNull Long entidadId,
            @NotNull Long credencialId,
            @NotNull LocalDate fecha,
            @NotNull LocalDate vigenciaHasta
    ) {
    }
}
