package com.bancotiempo.pasaportesaberes.infrastructure.config;

import com.bancotiempo.pasaportesaberes.application.port.in.AdministracionUseCases;
import com.bancotiempo.pasaportesaberes.application.port.in.AgoraUseCases;
import com.bancotiempo.pasaportesaberes.application.port.in.CatalogoUseCases;
import com.bancotiempo.pasaportesaberes.application.port.in.CuentasUseCases;
import com.bancotiempo.pasaportesaberes.application.port.in.HorasUseCases;
import com.bancotiempo.pasaportesaberes.application.port.in.InstitucionesUseCases;
import com.bancotiempo.pasaportesaberes.application.port.in.PasaporteUseCases;
import com.bancotiempo.pasaportesaberes.application.port.in.ReputacionUseCases;
import com.bancotiempo.pasaportesaberes.application.port.in.SesionesUseCases;
import com.bancotiempo.pasaportesaberes.application.port.out.AlmacenEvidencias;
import com.bancotiempo.pasaportesaberes.application.port.out.ConsultasAgora;
import com.bancotiempo.pasaportesaberes.application.port.out.ConsultasImpacto;
import com.bancotiempo.pasaportesaberes.application.port.out.ConsultasPasaporte;
import com.bancotiempo.pasaportesaberes.application.port.out.DesbloqueadorPersona;
import com.bancotiempo.pasaportesaberes.application.port.out.FirmadorCredenciales;
import com.bancotiempo.pasaportesaberes.application.port.out.GeneradorCertificadoPdf;
import com.bancotiempo.pasaportesaberes.application.port.out.GeneradorCodigoVerificacion;
import com.bancotiempo.pasaportesaberes.application.port.out.GeneradorHmacDocumento;
import com.bancotiempo.pasaportesaberes.application.port.out.GeneradorToken;
import com.bancotiempo.pasaportesaberes.application.port.out.HasheadorContrasena;
import com.bancotiempo.pasaportesaberes.application.port.out.NotificadorInstitucional;
import com.bancotiempo.pasaportesaberes.application.port.out.NotificadorTope;
import com.bancotiempo.pasaportesaberes.application.port.out.PoliticaFondo;
import com.bancotiempo.pasaportesaberes.application.port.out.PoliticaTope;
import com.bancotiempo.pasaportesaberes.application.port.out.Reloj;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioAval;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioBilletera;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioCredencial;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioEvidencia;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioHabilidad;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioOferta;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioQuererAprender;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioRango;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioReporte;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioResena;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioSesion;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioTransaccion;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.application.port.out.UnidadDeTrabajo;
import com.bancotiempo.pasaportesaberes.application.port.out.VerificadorCredenciales;
import com.bancotiempo.pasaportesaberes.application.service.AdministracionService;
import com.bancotiempo.pasaportesaberes.application.service.AgoraService;
import com.bancotiempo.pasaportesaberes.application.service.CatalogoService;
import com.bancotiempo.pasaportesaberes.application.service.CuentasService;
import com.bancotiempo.pasaportesaberes.application.service.HorasService;
import com.bancotiempo.pasaportesaberes.application.service.InstitucionesService;
import com.bancotiempo.pasaportesaberes.application.service.PasaporteService;
import com.bancotiempo.pasaportesaberes.application.service.ReputacionService;
import com.bancotiempo.pasaportesaberes.application.service.SesionesService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Duration;

@Configuration
public class ConfiguracionApplication {
    @Bean
    CuentasUseCases cuentasUseCases(
            RepositorioUsuario usuarios,
            RepositorioBilletera billeteras,
            RepositorioTransaccion transacciones,
            HasheadorContrasena contrasenas,
            GeneradorHmacDocumento documentos,
            GeneradorToken tokens,
            UnidadDeTrabajo unidadDeTrabajo,
            Reloj reloj,
            @Value("${pasaporte-saberes.cuentas.bono-inicial}") BigDecimal bonoInicial
    ) {
        return new CuentasService(
                usuarios,
                billeteras,
                transacciones,
                contrasenas,
                documentos,
                tokens,
                unidadDeTrabajo,
                reloj,
                bonoInicial
        );
    }

    @Bean
    CatalogoUseCases catalogoUseCases(
            RepositorioUsuario usuarios,
            RepositorioHabilidad habilidades,
            RepositorioOferta ofertas,
            RepositorioSesion sesiones,
            RepositorioQuererAprender intereses,
            UnidadDeTrabajo unidadDeTrabajo,
            Reloj reloj
    ) {
        return new CatalogoService(
                usuarios, habilidades, ofertas, sesiones, intereses, unidadDeTrabajo, reloj);
    }

    @Bean
    AgoraUseCases agoraUseCases(
            ConsultasAgora consultas,
            RepositorioUsuario usuarios
    ) {
        return new AgoraService(consultas, usuarios);
    }

    @Bean
    HorasUseCases horasUseCases(
            RepositorioUsuario usuarios,
            RepositorioBilletera billeteras,
            RepositorioTransaccion transacciones,
            PoliticaFondo politicaFondo,
            UnidadDeTrabajo unidadDeTrabajo,
            Reloj reloj
    ) {
        return new HorasService(
                usuarios, billeteras, transacciones, politicaFondo, unidadDeTrabajo, reloj);
    }

    @Bean
    SesionesUseCases sesionesUseCases(
            RepositorioUsuario usuarios,
            RepositorioOferta ofertas,
            RepositorioSesion sesiones,
            RepositorioBilletera billeteras,
            RepositorioTransaccion transacciones,
            RepositorioEvidencia evidencias,
            RepositorioReporte reportes,
            GeneradorToken generadorToken,
            AlmacenEvidencias almacenEvidencias,
            PoliticaTope politicaTope,
            NotificadorTope notificadorTope,
            UnidadDeTrabajo unidadDeTrabajo,
            Reloj reloj,
            PasaporteUseCases pasaporte,
            @Value("${pasaporte-saberes.sesiones.vigencia-handshake}") Duration vigenciaHandshake
    ) {
        return new SesionesService(
                usuarios,
                ofertas,
                sesiones,
                billeteras,
                transacciones,
                evidencias,
                reportes,
                generadorToken,
                almacenEvidencias,
                politicaTope,
                notificadorTope,
                unidadDeTrabajo,
                reloj,
                pasaporte,
                vigenciaHandshake
        );
    }

    @Bean
    ReputacionUseCases reputacionUseCases(
            RepositorioUsuario usuarios,
            RepositorioSesion sesiones,
            RepositorioResena resenas,
            UnidadDeTrabajo unidadDeTrabajo,
            Reloj reloj,
            PasaporteUseCases pasaporte
    ) {
        return new ReputacionService(
                usuarios, sesiones, resenas, unidadDeTrabajo, reloj, pasaporte);
    }

    @Bean
    PasaporteUseCases pasaporteUseCases(
            RepositorioUsuario usuarios,
            RepositorioCredencial credenciales,
            RepositorioAval avales,
            RepositorioRango rangos,
            RepositorioResena resenas,
            ConsultasPasaporte consultas,
            GeneradorCodigoVerificacion codigos,
            FirmadorCredenciales firmador,
            VerificadorCredenciales verificador,
            GeneradorCertificadoPdf pdf,
            UnidadDeTrabajo unidadDeTrabajo,
            Reloj reloj,
            @Value("${pasaporte-saberes.pasaporte.rating-minimo-validacion}")
            BigDecimal ratingMinimoValidacion,
            @Value("${pasaporte-saberes.pasaporte.aprendices-minimos-validacion}")
            int aprendicesMinimosValidacion
    ) {
        return new PasaporteService(
                usuarios,
                credenciales,
                avales,
                rangos,
                resenas,
                consultas,
                codigos,
                firmador,
                verificador,
                pdf,
                unidadDeTrabajo,
                reloj,
                ratingMinimoValidacion,
                aprendicesMinimosValidacion
        );
    }

    @Bean
    InstitucionesUseCases institucionesUseCases(
            RepositorioUsuario usuarios,
            RepositorioCredencial credenciales,
            RepositorioAval avales,
            NotificadorInstitucional notificador,
            UnidadDeTrabajo unidadDeTrabajo,
            Reloj reloj
    ) {
        return new InstitucionesService(
                usuarios, credenciales, avales, notificador, unidadDeTrabajo, reloj);
    }

    @Bean
    AdministracionUseCases administracionUseCases(
            RepositorioUsuario usuarios,
            RepositorioReporte reportes,
            ConsultasImpacto consultasImpacto,
            DesbloqueadorPersona desbloqueador,
            UnidadDeTrabajo unidadDeTrabajo
    ) {
        return new AdministracionService(
                usuarios, reportes, consultasImpacto, desbloqueador, unidadDeTrabajo);
    }
}
