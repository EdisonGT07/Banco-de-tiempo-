package com.bancotiempo.pasaportesaberes.infrastructure.adapter.in.rest;

import com.bancotiempo.pasaportesaberes.application.port.in.CuentasUseCases;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.LocalDate;

@RestController
@Validated
@RequestMapping("/api/v1/cuentas")
public class CuentasRestController {
    private final CuentasUseCases cuentas;
    private final Clock clock;

    public CuentasRestController(CuentasUseCases cuentas, Clock clock) {
        this.cuentas = cuentas;
        this.clock = clock;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public CuentasUseCases.Perfil registrar(@Valid @RequestBody RegistroRequest request) {
        return cuentas.registrarUsuario(new CuentasUseCases.RegistroUsuario(
                request.nombre(),
                request.correo(),
                request.contrasena(),
                request.documento(),
                request.fechaNacimiento(),
                request.zona(),
                request.estrato(),
                OffsetDateTime.now(clock).withOffsetSameInstant(ZoneOffset.UTC)
        ));
    }

    @PostMapping("/login")
    public CuentasUseCases.SesionAutenticada iniciarSesion(
            @Valid @RequestBody LoginRequest request
    ) {
        return cuentas.iniciarSesion(
                new CuentasUseCases.CredencialesInicioSesion(
                        request.correo(),
                        request.contrasena()
                )
        );
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public CuentasUseCases.Perfil miPerfil(@AuthenticationPrincipal Jwt jwt) {
        return cuentas.obtenerMiPerfil(IdentidadAutenticada.usuarioId(jwt));
    }

    public record RegistroRequest(
            @NotBlank String nombre,
            @NotBlank @Email String correo,
            @NotBlank String contrasena,
            @NotBlank String documento,
            @NotNull @Past LocalDate fechaNacimiento,
            Persona.Zona zona,
            @Min(1) @Max(6) Short estrato,
            @NotNull @AssertTrue Boolean aceptaHabeasData
    ) {
    }

    public record LoginRequest(
            @NotBlank @Email String correo,
            @NotBlank String contrasena
    ) {
    }
}
