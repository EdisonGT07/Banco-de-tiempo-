package com.bancotiempo.pasaportesaberes.infrastructure.adapter.in.rest;

import com.bancotiempo.pasaportesaberes.application.ErrorAplicacionException;
import com.bancotiempo.pasaportesaberes.domain.exception.DomainException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler {
    private final Clock clock;

    public RestExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(ErrorAplicacionException.class)
    ResponseEntity<ErrorResponse> manejarErrorAplicacion(
            ErrorAplicacionException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = switch (exception.getCodigo()) {
            case NO_ENCONTRADO -> HttpStatus.NOT_FOUND;
            case CONFLICTO -> HttpStatus.CONFLICT;
            case NO_AUTORIZADO -> "/api/v1/cuentas/login".equals(request.getRequestURI())
                    ? HttpStatus.UNAUTHORIZED
                    : HttpStatus.FORBIDDEN;
            case VALIDACION -> HttpStatus.BAD_REQUEST;
        };
        return respuesta(status, exception.getCodigo().name(), exception.getMessage(), request);
    }

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ErrorResponse> manejarErrorDominio(
            DomainException exception,
            HttpServletRequest request
    ) {
        return respuesta(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "REGLA_DE_DOMINIO",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> manejarValidacion(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String mensaje = exception.getBindingResult().getFieldErrors().stream()
                .map(RestExceptionHandler::mensajeCampo)
                .distinct()
                .collect(Collectors.joining("; "));
        return respuesta(HttpStatus.BAD_REQUEST, "VALIDACION", mensaje, request);
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            HandlerMethodValidationException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    ResponseEntity<ErrorResponse> manejarSolicitudInvalida(
            Exception exception,
            HttpServletRequest request
    ) {
        return respuesta(
                HttpStatus.BAD_REQUEST,
                "SOLICITUD_INVALIDA",
                "La solicitud tiene campos ausentes o un formato inválido",
                request
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ErrorResponse> manejarArchivoDemasiadoGrande(
            MaxUploadSizeExceededException exception,
            HttpServletRequest request
    ) {
        return respuesta(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "ARCHIVO_DEMASIADO_GRANDE",
                "El archivo supera el tamaño máximo permitido",
                request
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponse> manejarConflictoPersistencia(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return respuesta(
                HttpStatus.CONFLICT,
                "CONFLICTO_PERSISTENCIA",
                "La operación entra en conflicto con los datos existentes",
                request
        );
    }

    private ResponseEntity<ErrorResponse> respuesta(
            HttpStatus status,
            String codigo,
            String mensaje,
            HttpServletRequest request
    ) {
        ErrorResponse body = new ErrorResponse(
                OffsetDateTime.now(clock),
                status.value(),
                codigo,
                mensaje,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }

    private static String mensajeCampo(FieldError error) {
        return "El campo '" + error.getField() + "' no es válido";
    }

    public record ErrorResponse(
            OffsetDateTime timestamp,
            int status,
            String codigo,
            String mensaje,
            String ruta
    ) {
    }
}
