package com.bancotiempo.pasaportesaberes.infrastructure.adapter.in.rest;

import com.bancotiempo.pasaportesaberes.application.port.in.CatalogoUseCases;
import com.bancotiempo.pasaportesaberes.domain.model.Oferta;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogo")
@SecurityRequirement(name = "bearerAuth")
public class CatalogoRestController {
    private final CatalogoUseCases catalogo;

    public CatalogoRestController(CatalogoUseCases catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping("/habilidades")
    public List<CatalogoUseCases.HabilidadPublica> listarHabilidades() {
        return catalogo.listarHabilidades();
    }

    @PostMapping("/habilidades")
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogoUseCases.HabilidadPublica registrarHabilidad(
            @Valid @RequestBody NuevaHabilidadRequest request
    ) {
        return catalogo.registrarHabilidad(
                new CatalogoUseCases.NuevaHabilidad(
                        request.nombre(),
                        request.categoria()));
    }

    @PostMapping("/ofertas")
    @ResponseStatus(HttpStatus.CREATED)
    public RespuestasApi.OfertaResponse publicarOferta(
            @Valid @RequestBody PublicarOfertaRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Oferta oferta = catalogo.publicarOferta(new CatalogoUseCases.PublicarOferta(
                IdentidadAutenticada.usuarioId(jwt),
                request.habilidadId(),
                request.descripcion(),
                request.duracionHoras(),
                request.modalidad(),
                request.barrio(),
                request.destinoHoras(),
                request.receptorRegaloId()
        ));
        return RespuestasApi.oferta(oferta);
    }

    @PostMapping("/sesiones")
    @ResponseStatus(HttpStatus.CREATED)
    public RespuestasApi.SesionResponse crearSesion(
            @Valid @RequestBody CrearSesionRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return RespuestasApi.sesion(catalogo.crearSesion(new CatalogoUseCases.CrearSesion(
                IdentidadAutenticada.usuarioId(jwt),
                request.ofertaId(),
                request.iniciaEn(),
                request.lugarOEnlace()
        )));
    }

    @PostMapping("/solicitudes-aprendizaje")
    @ResponseStatus(HttpStatus.CREATED)
    public RespuestasApi.SolicitudAprendizajeResponse solicitarAprendizaje(
            @Valid @RequestBody SolicitudAprendizajeRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return RespuestasApi.solicitudAprendizaje(
                catalogo.registrarSolicitudAprendizaje(
                        new CatalogoUseCases.RegistrarSolicitudAprendizaje(
                                IdentidadAutenticada.usuarioId(jwt),
                                request.habilidadId()
                        )
                )
        );
    }

    public record PublicarOfertaRequest(
            @NotNull Long habilidadId,
            @NotBlank String descripcion,
            @NotNull @DecimalMin("0.01") @Digits(integer = 2, fraction = 2)
            BigDecimal duracionHoras,
            @NotNull Oferta.Modalidad modalidad,
            String barrio,
            @NotNull Oferta.DestinoHoras destinoHoras,
            Long receptorRegaloId
    ) {
    }

    public record CrearSesionRequest(
            @NotNull Long ofertaId,
            @NotNull OffsetDateTime iniciaEn,
            @NotBlank String lugarOEnlace
    ) {
    }

    public record SolicitudAprendizajeRequest(@NotNull Long habilidadId) {
    }

    public record NuevaHabilidadRequest(
            @NotBlank String nombre,
            @NotBlank String categoria
    ) {
    }
}
