package com.bancotiempo.pasaportesaberes.infrastructure.adapter.in.rest;

import com.bancotiempo.pasaportesaberes.application.port.in.HorasUseCases;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/horas")
@SecurityRequirement(name = "bearerAuth")
public class HorasRestController {
    private final HorasUseCases horas;

    public HorasRestController(HorasUseCases horas) {
        this.horas = horas;
    }

    @GetMapping("/billetera")
    public RespuestasApi.BilleteraResponse consultarBilletera(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return RespuestasApi.billetera(
                horas.consultarBilletera(IdentidadAutenticada.usuarioId(jwt)));
    }

    @PostMapping("/retiros-fondo")
    @ResponseStatus(HttpStatus.CREATED)
    public RespuestasApi.TransaccionResponse retirarDelFondo(
            @Valid @RequestBody RetiroRequest request,
            @RequestHeader("Idempotency-Key") String claveIdempotencia,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return RespuestasApi.transaccion(horas.retirarDelFondo(
                new HorasUseCases.RetiroFondo(
                        IdentidadAutenticada.usuarioId(jwt),
                        request.horas(),
                        claveIdempotencia
                )
        ));
    }

    public record RetiroRequest(
            @NotNull @DecimalMin("0.01") @Digits(integer = 6, fraction = 2)
            BigDecimal horas
    ) {
    }
}
