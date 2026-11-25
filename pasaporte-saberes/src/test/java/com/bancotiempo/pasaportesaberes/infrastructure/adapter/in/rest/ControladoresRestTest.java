package com.bancotiempo.pasaportesaberes.infrastructure.adapter.in.rest;

import com.bancotiempo.pasaportesaberes.application.port.in.AdministracionUseCases;
import com.bancotiempo.pasaportesaberes.application.port.in.AgoraUseCases;
import com.bancotiempo.pasaportesaberes.application.port.in.CatalogoUseCases;
import com.bancotiempo.pasaportesaberes.application.port.in.CuentasUseCases;
import com.bancotiempo.pasaportesaberes.application.port.in.HorasUseCases;
import com.bancotiempo.pasaportesaberes.application.port.in.InstitucionesUseCases;
import com.bancotiempo.pasaportesaberes.application.port.in.PasaporteUseCases;
import com.bancotiempo.pasaportesaberes.application.port.in.ReputacionUseCases;
import com.bancotiempo.pasaportesaberes.application.port.in.SesionesUseCases;
import com.bancotiempo.pasaportesaberes.domain.model.Administrador;
import com.bancotiempo.pasaportesaberes.domain.model.Aval;
import com.bancotiempo.pasaportesaberes.domain.model.Billetera;
import com.bancotiempo.pasaportesaberes.domain.model.Credencial;
import com.bancotiempo.pasaportesaberes.domain.model.EntidadAliada;
import com.bancotiempo.pasaportesaberes.domain.model.Evidencia;
import com.bancotiempo.pasaportesaberes.domain.model.Habilidad;
import com.bancotiempo.pasaportesaberes.domain.model.Oferta;
import com.bancotiempo.pasaportesaberes.domain.model.Persona;
import com.bancotiempo.pasaportesaberes.domain.model.QuererAprender;
import com.bancotiempo.pasaportesaberes.domain.model.QuererAprenderId;
import com.bancotiempo.pasaportesaberes.domain.model.Reporte;
import com.bancotiempo.pasaportesaberes.domain.model.Resena;
import com.bancotiempo.pasaportesaberes.domain.model.Sesion;
import com.bancotiempo.pasaportesaberes.domain.model.Transaccion;
import com.bancotiempo.pasaportesaberes.infrastructure.config.ConfiguracionSeguridad;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockPart;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AdministracionRestController.class,
        AgoraRestController.class,
        CatalogoRestController.class,
        CuentasRestController.class,
        HorasRestController.class,
        InstitucionesRestController.class,
        PasaporteRestController.class,
        ReputacionRestController.class,
        SesionesRestController.class
})
@Import({ConfiguracionSeguridad.class, RestExceptionHandler.class})
@TestPropertySource(properties = {
        "security.jwt.secret-base64=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "security.jwt.access-token-ttl=PT15M"
})
class ControladoresRestTest {
    private static final OffsetDateTime AHORA =
            OffsetDateTime.parse("2026-11-25T13:00:00Z");
    private static final Long ACTOR_ID = 42L;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdministracionUseCases administracion;

    @MockitoBean
    private AgoraUseCases agora;

    @MockitoBean
    private CatalogoUseCases catalogo;

    @MockitoBean
    private CuentasUseCases cuentas;

    @MockitoBean
    private HorasUseCases horas;

    @MockitoBean
    private InstitucionesUseCases instituciones;

    @MockitoBean
    private PasaporteUseCases pasaporte;

    @MockitoBean
    private ReputacionUseCases reputacion;

    @MockitoBean
    private SesionesUseCases sesiones;

    @Test
    void cuentasExponeRegistroInicioDeSesionYPerfil() throws Exception {
        when(cuentas.registrarUsuario(any())).thenReturn(perfilPersona(ACTOR_ID));
        when(cuentas.iniciarSesion(any())).thenReturn(
                new CuentasUseCases.SesionAutenticada("jwt-test", perfilPersona(ACTOR_ID)));
        when(cuentas.obtenerMiPerfil(ACTOR_ID)).thenReturn(perfilPersona(ACTOR_ID));

        mockMvc.perform(post("/api/v1/cuentas/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Ada",
                                  "correo": "ada@example.com",
                                  "contrasena": "secreto",
                                  "documento": "123456",
                                  "fechaNacimiento": "1990-01-01",
                                  "zona": "URBANA",
                                  "estrato": 2,
                                  "aceptaHabeasData": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoCuenta").value("PERSONA"));

        mockMvc.perform(post("/api/v1/cuentas/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"ada@example.com","contrasena":"secreto"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-test"));

        mockMvc.perform(get("/api/v1/cuentas/me")
                        .with(jwt().jwt(token -> token.subject(ACTOR_ID.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ACTOR_ID));

        verify(cuentas).registrarUsuario(argThat(request ->
                request.correo().equals("ada@example.com")
                        && request.aceptaHabeasDataEn() != null));
        verify(cuentas).iniciarSesion(argThat(request ->
                request.correo().equals("ada@example.com")));
        verify(cuentas).obtenerMiPerfil(ACTOR_ID);
    }

    @Test
    void catalogoPublicaOfertasCreaSesionesYRegistraIntereses() throws Exception {
        Oferta oferta = oferta();
        Sesion sesionProgramada = sesion(
                51L,
                Sesion.EstadoSesion.PROGRAMADA,
                null
        );
        QuererAprender quererAprender = new QuererAprender(
                new QuererAprenderId(ACTOR_ID, 7L),
                persona(ACTOR_ID),
                habilidad(),
                AHORA
        );
        when(catalogo.publicarOferta(any())).thenReturn(oferta);
        when(catalogo.crearSesion(any())).thenReturn(sesionProgramada);
        when(catalogo.registrarSolicitudAprendizaje(any())).thenReturn(quererAprender);

        mockMvc.perform(post("/api/v1/catalogo/ofertas")
                        .with(jwtConId(ACTOR_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "habilidadId":7,
                                  "descripcion":"Clases de guitarra",
                                  "duracionHoras":1.5,
                                  "modalidad":"VIRTUAL",
                                  "destinoHoras":"BILLETERA_PROPIA"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(50))
                .andExpect(jsonPath("$.tutorId").value(ACTOR_ID));

        mockMvc.perform(post("/api/v1/catalogo/sesiones")
                        .with(jwtConId(ACTOR_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ofertaId":50,
                                  "iniciaEn":"2026-11-26T13:00:00Z",
                                  "lugarOEnlace":"https://meet.example.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(51));

        mockMvc.perform(post("/api/v1/catalogo/solicitudes-aprendizaje")
                        .with(jwtConId(ACTOR_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"habilidadId":7}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.personaId").value(ACTOR_ID));

        verify(catalogo).publicarOferta(argThat(request ->
                request.tutorId().equals(ACTOR_ID)));
        verify(catalogo).crearSesion(argThat(request ->
                request.tutorId().equals(ACTOR_ID)));
        verify(catalogo).registrarSolicitudAprendizaje(argThat(request ->
                request.personaId().equals(ACTOR_ID)));
    }

    @Test
    void agoraBuscaOfertasYObtieneSugerenciasDelActorAutenticado() throws Exception {
        when(agora.buscarOfertasEnAgora(any())).thenReturn(List.of(
                new AgoraUseCases.TarjetaOferta(
                        50L,
                        17L,
                        "Tutora",
                        "Guitarra",
                        "Clases de guitarra",
                        new BigDecimal("1.50"),
                        Oferta.Modalidad.VIRTUAL,
                        null,
                        new BigDecimal("4.80"),
                        "Avanzado",
                        null
                )
        ));
        when(agora.sugerirEmparejamientos(ACTOR_ID)).thenReturn(List.of(
                new AgoraUseCases.SugerenciaEmparejamiento(
                        ACTOR_ID, 50L, 17L, 7L, true)
        ));

        mockMvc.perform(get("/api/v1/agora/ofertas")
                        .with(jwtConId(ACTOR_ID))
                        .param("texto", "guitarra")
                        .param("modalidad", "VIRTUAL")
                        .param("pagina", "1")
                        .param("tamanoPagina", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].habilidad").value("Guitarra"));

        mockMvc.perform(get("/api/v1/agora/sugerencias")
                        .with(jwtConId(ACTOR_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].personaId").value(ACTOR_ID));

        verify(agora).buscarOfertasEnAgora(argThat(filtros ->
                filtros.texto().equals("guitarra")
                        && filtros.pagina() == 1
                        && filtros.tamanoPagina() == 10));
        verify(agora).sugerirEmparejamientos(ACTOR_ID);
    }

    @Test
    void sesionesCubreInscripcionHandshakeFinYReporte() throws Exception {
        Persona aprendiz = persona(ACTOR_ID);
        Sesion reservada = sesion(51L, Sesion.EstadoSesion.RESERVADA, aprendiz);
        Sesion cancelada = sesion(51L, Sesion.EstadoSesion.CANCELADA, aprendiz);
        Sesion enCurso = sesion(51L, Sesion.EstadoSesion.EN_CURSO, aprendiz);
        Sesion completada = sesion(51L, Sesion.EstadoSesion.COMPLETADA, aprendiz);
        when(sesiones.inscribirseEnSesion(ACTOR_ID, 51L)).thenReturn(reservada);
        when(sesiones.cancelarInscripcion(ACTOR_ID, 51L)).thenReturn(cancelada);
        when(sesiones.iniciarHandshake(ACTOR_ID, 51L)).thenReturn(
                new SesionesUseCases.Handshake(evidencia(reservada), "token-seguro", "123456"));
        when(sesiones.confirmarHandshake(any())).thenReturn(enCurso);
        when(sesiones.confirmarFinSesion(ACTOR_ID, 51L)).thenReturn(completada);
        when(sesiones.reportarProblema(any())).thenReturn(reporte(aprendiz));

        mockMvc.perform(post("/api/v1/sesiones/51/inscripcion")
                        .with(jwtConId(ACTOR_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.aprendizId").value(ACTOR_ID));

        mockMvc.perform(delete("/api/v1/sesiones/51/inscripcion")
                        .with(jwtConId(ACTOR_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        mockMvc.perform(post("/api/v1/sesiones/51/handshake")
                        .with(jwtConId(ACTOR_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token-seguro"));

        MockPart solicitudHandshake = new MockPart(
                "solicitud",
                """
                        {"token":"token-seguro","consienteEvidencia":false}
                        """.getBytes()
        );
        solicitudHandshake.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(multipart("/api/v1/sesiones/51/confirmacion-handshake")
                        .part(solicitudHandshake)
                        .with(jwtConId(ACTOR_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_CURSO"));

        mockMvc.perform(post("/api/v1/sesiones/51/confirmacion-fin")
                        .with(jwtConId(ACTOR_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("COMPLETADA"));

        mockMvc.perform(post("/api/v1/sesiones/51/reportes")
                        .with(jwtConId(ACTOR_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"motivo":"La sesión no se realizó"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.autorId").value(ACTOR_ID));

        verify(sesiones).inscribirseEnSesion(ACTOR_ID, 51L);
        verify(sesiones).cancelarInscripcion(ACTOR_ID, 51L);
        verify(sesiones).iniciarHandshake(ACTOR_ID, 51L);
        verify(sesiones).confirmarHandshake(argThat(request ->
                request.aprendizId().equals(ACTOR_ID)
                        && request.sesionId().equals(51L)
                        && request.token().equals("token-seguro")));
        verify(sesiones).confirmarFinSesion(ACTOR_ID, 51L);
        verify(sesiones).reportarProblema(argThat(request ->
                request.autorId().equals(ACTOR_ID)));
    }

    @Test
    void horasConsultaLaBilleteraYRetiraConClaveDeIdempotencia() throws Exception {
        Billetera wallet = new Billetera(
                20L,
                Billetera.TipoBilletera.USUARIO,
                persona(ACTOR_ID),
                new BigDecimal("3.00"),
                BigDecimal.ZERO,
                0
        );
        Billetera fondo = new Billetera(
                21L,
                Billetera.TipoBilletera.FONDO,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0
        );
        Transaccion retiro = new Transaccion(
                90L,
                Transaccion.TipoTransaccion.RETIRO_FONDO,
                BigDecimal.ONE,
                fondo,
                wallet,
                null,
                "retiro-prueba",
                AHORA
        );
        when(horas.consultarBilletera(ACTOR_ID)).thenReturn(wallet);
        when(horas.retirarDelFondo(any())).thenReturn(retiro);

        mockMvc.perform(get("/api/v1/horas/billetera")
                        .with(jwtConId(ACTOR_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoDisponible").value(3.0));

        mockMvc.perform(post("/api/v1/horas/retiros-fondo")
                        .with(jwtConId(ACTOR_ID))
                        .header("Idempotency-Key", "retiro-prueba")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"horas":1.00}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("RETIRO_FONDO"));

        verify(horas).consultarBilletera(ACTOR_ID);
        verify(horas).retirarDelFondo(argThat(request ->
                request.personaId().equals(ACTOR_ID)
                        && request.claveIdempotencia().equals("retiro-prueba")));
    }

    @Test
    void reputacionCalificaUnaSesionYConsultaElPromedio() throws Exception {
        Resena resena = new Resena(
                70L,
                sesion(51L, Sesion.EstadoSesion.COMPLETADA, persona(ACTOR_ID)),
                Resena.AutorRol.APRENDIZ,
                (short) 5,
                "Excelente",
                AHORA
        );
        when(reputacion.calificarSesion(any())).thenReturn(resena);
        when(reputacion.obtenerReputacion(17L, 7L)).thenReturn(
                new ReputacionUseCases.Reputacion(
                        new BigDecimal("4.80"),
                        3,
                        8
                )
        );

        mockMvc.perform(post("/api/v1/sesiones/51/resenas")
                        .with(jwtConId(ACTOR_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"calificacion":5,"comentario":"Excelente"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.autorId").value(ACTOR_ID));

        mockMvc.perform(get(
                        "/api/v1/personas/17/habilidades/7/reputacion")
                        .with(jwtConId(ACTOR_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promedioPonderado").value(4.8));

        verify(reputacion).calificarSesion(argThat(request ->
                request.autorId().equals(ACTOR_ID)
                        && request.calificacion() == 5));
        verify(reputacion).obtenerReputacion(17L, 7L);
    }

    @Test
    void pasaporteExponeConsultaVerificacionRangosYDescargaPdf() throws Exception {
        PasaporteUseCases.CredencialPublica credencialPublica =
                new PasaporteUseCases.CredencialPublica(
                        80L,
                        "Guitarra",
                        new BigDecimal("12.00"),
                        new BigDecimal("4.80"),
                        Credencial.Nivel.VALIDADA_POR_PARES,
                        null
                );
        when(pasaporte.obtenerPasaporte(17L)).thenReturn(
                new PasaporteUseCases.Pasaporte(
                        17L,
                        "Tutora",
                        List.of(credencialPublica)
                )
        );
        when(pasaporte.verificarCredencial("COD-80")).thenReturn(
                new PasaporteUseCases.Verificacion(credencialPublica, true, 4)
        );
        when(pasaporte.calcularRangos(ACTOR_ID)).thenReturn(
                new PasaporteUseCases.Rangos(null, null)
        );
        when(pasaporte.emitirCertificadoPdf(80L)).thenReturn(
                new byte[]{'%','P','D','F'}
        );

        mockMvc.perform(get("/api/v1/pasaportes/personas/17")
                        .with(jwtConId(ACTOR_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.credenciales[0].habilidad").value("Guitarra"));

        mockMvc.perform(get("/api/v1/pasaportes/verificar/COD-80"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firmaValida").value(true));

        mockMvc.perform(post("/api/v1/pasaportes/personas/me/rangos/recalcular")
                        .with(jwtConId(ACTOR_ID)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/pasaportes/credenciales/80/certificado.pdf")
                        .with(jwtConId(ACTOR_ID)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"certificado-80.pdf\""));

        verify(pasaporte).obtenerPasaporte(17L);
        verify(pasaporte).verificarCredencial("COD-80");
        verify(pasaporte).calcularRangos(ACTOR_ID);
        verify(pasaporte).emitirCertificadoPdf(80L);
    }

    @Test
    void institucionesSolicitaYRegistraAvalConRolesCorrespondientes() throws Exception {
        EntidadAliada entidad = new EntidadAliada(
                19L,
                "Fundación",
                "fundacion@example.com",
                "hash",
                AHORA,
                "EDUCATIVA",
                "INST-19"
        );
        Credencial credencial = new Credencial(
                80L,
                persona(17L),
                habilidad(),
                new BigDecimal("12.00"),
                new BigDecimal("4.80"),
                "COD-80",
                "firma",
                AHORA,
                0
        );
        Aval aval = new Aval(
                81L,
                entidad,
                credencial,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 1, 1),
                Aval.EstadoAval.VIGENTE
        );
        when(instituciones.registrarAval(any())).thenReturn(aval);

        mockMvc.perform(post(
                        "/api/v1/personas/me/credenciales/80/solicitud-aval")
                        .with(jwtConId(ACTOR_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"entidadId":19}
                                """))
                .andExpect(status().isAccepted());

        mockMvc.perform(post("/api/v1/instituciones/avales")
                        .with(jwtAdmin(91L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "entidadId":19,
                                  "credencialId":80,
                                  "fecha":"2026-01-01",
                                  "vigenciaHasta":"2027-01-01"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entidadId").value(19));

        mockMvc.perform(post("/api/v1/instituciones/avales")
                        .with(jwtEntidad(19L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "entidadId":19,
                                  "credencialId":80,
                                  "fecha":"2026-01-01",
                                  "vigenciaHasta":"2027-01-01"
                                }
                                """))
                .andExpect(status().isForbidden());

        verify(instituciones).solicitarAval(argThat(request ->
                request.personaId().equals(ACTOR_ID)
                        && request.credencialId().equals(80L)));
        verify(instituciones).registrarAval(argThat(request ->
                request.administradorId().equals(91L)));
    }

    @Test
    void administracionRequiereRolAdminYExponeLasOperaciones() throws Exception {
        Persona personaDesbloqueada = persona(17L);
        when(administracion.obtenerPanelImpacto()).thenReturn(
                new AdministracionUseCases.PanelImpacto(
                        new BigDecimal("18.00"),
                        8,
                        6,
                        4,
                        3
                )
        );
        when(administracion.revisarReporte(any())).thenReturn(reporte(persona(ACTOR_ID)));
        when(administracion.desbloquearUsuario(any())).thenReturn(personaDesbloqueada);

        mockMvc.perform(get("/api/v1/admin/impacto")
                        .with(jwtAdmin(91L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horasIntercambiadas").value(18.0));

        mockMvc.perform(patch("/api/v1/admin/reportes/60")
                        .with(jwtAdmin(91L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"resolver":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(60));

        mockMvc.perform(post("/api/v1/admin/personas/17/desbloqueo")
                        .with(jwtAdmin(91L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(17));

        mockMvc.perform(get("/api/v1/admin/impacto")
                        .with(jwtConId(ACTOR_ID)))
                .andExpect(status().isForbidden());

        verify(administracion).obtenerPanelImpacto();
        verify(administracion).revisarReporte(argThat(request ->
                request.administradorId().equals(91L)
                        && request.reporteId().equals(60L)));
        verify(administracion).desbloquearUsuario(argThat(request ->
                request.administradorId().equals(91L)
                        && request.personaId().equals(17L)));
    }

    @Test
    void endpointsPrivadosNoInvocanElCasoDeUsoSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/agora/ofertas"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/catalogo/solicitudes-aprendizaje")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"habilidadId":7}
                                """))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/pasaportes/personas/17"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(agora, catalogo, pasaporte);
    }

    private static CuentasUseCases.Perfil perfilPersona(Long id) {
        return new CuentasUseCases.Perfil(
                id,
                "Ada",
                "ada@example.com",
                LocalDate.of(1990, 1, 1),
                Persona.Zona.URBANA,
                (short) 2,
                Persona.Estado.ACTIVO
        );
    }

    private static Persona persona(Long id) {
        Persona persona = new Persona();
        persona.setId(id);
        persona.setNombre("Ada " + id);
        persona.setCorreo("persona" + id + "@example.com");
        persona.setContrasenaHash("hash");
        persona.setHabeasDataEn(AHORA);
        return persona;
    }

    private static Habilidad habilidad() {
        return new Habilidad(7L, "Guitarra", "Música");
    }

    private static Oferta oferta() {
        return oferta(ACTOR_ID);
    }

    private static Oferta oferta(Long tutorId) {
        return new Oferta(
                50L,
                persona(tutorId),
                habilidad(),
                "Clases de guitarra",
                new BigDecimal("1.50"),
                Oferta.Modalidad.VIRTUAL,
                null,
                Oferta.DestinoHoras.BILLETERA_PROPIA,
                null,
                true,
                AHORA
        );
    }

    private static Sesion sesion(
            Long id,
            Sesion.EstadoSesion estado,
            Persona aprendiz
    ) {
        OffsetDateTime confirmacionTutor =
                estado == Sesion.EstadoSesion.COMPLETADA ? AHORA : null;
        OffsetDateTime confirmacionAprendiz =
                estado == Sesion.EstadoSesion.COMPLETADA ? AHORA : null;
        return new Sesion(
                id,
                oferta(17L),
                aprendiz,
                AHORA.plusDays(1),
                "Enlace",
                estado,
                confirmacionTutor,
                confirmacionAprendiz,
                null,
                0,
                AHORA
        );
    }

    private static Evidencia evidencia(Sesion sesion) {
        return new Evidencia(
                88L,
                sesion,
                "a".repeat(64),
                "b".repeat(64),
                AHORA.plusMinutes(10),
                null,
                (short) 0,
                null,
                null,
                null,
                AHORA
        );
    }

    private static Reporte reporte(Persona autor) {
        return new Reporte(
                60L,
                sesion(Sesion.EstadoSesion.DISPUTADA, autor),
                autor,
                null,
                "La sesión no se realizó",
                Reporte.EstadoReporte.ABIERTO,
                AHORA
        );
    }

    private static Sesion sesion(
            Sesion.EstadoSesion estado,
            Persona aprendiz
    ) {
        return sesion(51L, estado, aprendiz);
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor jwtConId(
            Long id
    ) {
        return jwt().jwt(token -> token.subject(id.toString()));
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor jwtAdmin(
            Long id
    ) {
        return jwt().jwt(token -> token.subject(id.toString()))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor jwtEntidad(
            Long id
    ) {
        return jwt().jwt(token -> token.subject(id.toString()))
                .authorities(new SimpleGrantedAuthority("ROLE_ENTIDAD"));
    }
}
