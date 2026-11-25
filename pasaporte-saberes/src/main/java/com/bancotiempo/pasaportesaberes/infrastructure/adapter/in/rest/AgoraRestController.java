package com.bancotiempo.pasaportesaberes.infrastructure.adapter.in.rest;

import com.bancotiempo.pasaportesaberes.application.port.in.AgoraUseCases;
import com.bancotiempo.pasaportesaberes.domain.model.Oferta;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/agora")
@SecurityRequirement(name = "bearerAuth")
public class AgoraRestController {
    private final AgoraUseCases agora;

    public AgoraRestController(AgoraUseCases agora) {
        this.agora = agora;
    }

    @GetMapping("/ofertas")
    public List<AgoraUseCases.TarjetaOferta> buscarOfertas(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long habilidadId,
            @RequestParam(required = false) Oferta.Modalidad modalidad,
            @RequestParam(required = false) String barrio,
            @RequestParam(required = false) BigDecimal calificacionMinima,
            @RequestParam(required = false) Short ordenRangoMinimo,
            @RequestParam(defaultValue = "0") Integer pagina,
            @RequestParam(defaultValue = "20") Integer tamanoPagina
    ) {
        return agora.buscarOfertasEnAgora(new AgoraUseCases.FiltrosBusqueda(
                texto,
                habilidadId,
                modalidad,
                barrio,
                calificacionMinima,
                ordenRangoMinimo,
                pagina,
                tamanoPagina
        ));
    }

    @GetMapping("/sugerencias")
    public List<AgoraUseCases.SugerenciaEmparejamiento> sugerencias(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return agora.sugerirEmparejamientos(IdentidadAutenticada.usuarioId(jwt));
    }
}
