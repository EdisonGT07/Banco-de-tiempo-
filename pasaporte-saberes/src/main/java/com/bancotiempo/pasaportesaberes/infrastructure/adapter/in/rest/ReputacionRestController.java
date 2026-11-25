package com.bancotiempo.pasaportesaberes.infrastructure.adapter.in.rest;

import com.bancotiempo.pasaportesaberes.application.port.in.ReputacionUseCases;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearerAuth")
public class ReputacionRestController {
    private final ReputacionUseCases reputacion;

    public ReputacionRestController(ReputacionUseCases reputacion) {
        this.reputacion = reputacion;
    }

    @PostMapping("/sesiones/{sesionId}/resenas")
    @ResponseStatus(HttpStatus.CREATED)
    public RespuestasApi.ResenaResponse calificarSesion(
            @PathVariable Long sesionId,
            @Valid @RequestBody CalificarSesionRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return RespuestasApi.resena(reputacion.calificarSesion(
                new ReputacionUseCases.CalificarSesion(
                        IdentidadAutenticada.usuarioId(jwt),
                        sesionId,
                        request.calificacion(),
                        request.comentario()
                )
        ));
    }

    @GetMapping("/personas/{tutorId}/habilidades/{habilidadId}/reputacion")
    public ReputacionUseCases.Reputacion obtenerReputacion(
            @PathVariable Long tutorId,
            @PathVariable Long habilidadId
    ) {
        return reputacion.obtenerReputacion(tutorId, habilidadId);
    }

    public record CalificarSesionRequest(
            @NotNull @Min(1) @Max(5) Short calificacion,
            @Size(max = 2000) String comentario
    ) {
    }
}
