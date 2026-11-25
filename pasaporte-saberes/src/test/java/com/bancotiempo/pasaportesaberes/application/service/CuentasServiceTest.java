package com.bancotiempo.pasaportesaberes.application.service;

import com.bancotiempo.pasaportesaberes.application.ErrorAplicacionException;
import com.bancotiempo.pasaportesaberes.application.port.in.CuentasUseCases;
import com.bancotiempo.pasaportesaberes.application.port.out.GeneradorHmacDocumento;
import com.bancotiempo.pasaportesaberes.application.port.out.GeneradorToken;
import com.bancotiempo.pasaportesaberes.application.port.out.HasheadorContrasena;
import com.bancotiempo.pasaportesaberes.application.port.out.Reloj;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioBilletera;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioTransaccion;
import com.bancotiempo.pasaportesaberes.application.port.out.RepositorioUsuario;
import com.bancotiempo.pasaportesaberes.application.port.out.UnidadDeTrabajo;
import com.bancotiempo.pasaportesaberes.domain.model.Administrador;
import com.bancotiempo.pasaportesaberes.domain.model.EntidadAliada;
import com.bancotiempo.pasaportesaberes.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CuentasServiceTest {
    private final RepositorioUsuario usuarios = mock(RepositorioUsuario.class);
    private final GeneradorToken tokens = mock(GeneradorToken.class);
    private final HasheadorContrasena contrasenas = mock(HasheadorContrasena.class);
    private CuentasService servicio;

    @BeforeEach
    void setUp() {
        servicio = new CuentasService(
                usuarios,
                mock(RepositorioBilletera.class),
                mock(RepositorioTransaccion.class),
                contrasenas,
                mock(GeneradorHmacDocumento.class),
                tokens,
                new UnidadDeTrabajo() {
                    @Override
                    public <T> T ejecutar(Supplier<T> operacion) {
                        return operacion.get();
                    }
                },
                mock(Reloj.class),
                BigDecimal.ONE
        );
    }

    @Test
    void permiteIniciarSesionAUnaEntidadYDevuelveSuTipoDeCuenta() {
        EntidadAliada entidad = new EntidadAliada(
                17L,
                "Fundación",
                "fundacion@example.com",
                "hash",
                OffsetDateTime.parse("2026-01-01T00:00:00Z"),
                "EDUCATIVA",
                "INST-17"
        );
        prepararInicioSesion(entidad);

        CuentasUseCases.SesionAutenticada sesion = servicio.iniciarSesion(
                new CuentasUseCases.CredencialesInicioSesion(
                        "fundacion@example.com",
                        "secreto"
                )
        );

        assertThat(sesion.token()).isEqualTo("jwt-17");
        assertThat(sesion.perfil().tipoCuenta()).isEqualTo(CuentasUseCases.TipoCuenta.ENTIDAD);
        assertThat(sesion.perfil().tipoEntidad()).isEqualTo("EDUCATIVA");
        assertThat(sesion.perfil().codigoInstitucional()).isEqualTo("INST-17");
    }

    @Test
    void permiteIniciarSesionAUnAdministradorYDevuelveSuNivelDeAcceso() {
        Administrador administrador = new Administrador(
                23L,
                "Administración",
                "admin@example.com",
                "hash",
                OffsetDateTime.parse("2026-01-01T00:00:00Z"),
                (short) 2
        );
        prepararInicioSesion(administrador);

        CuentasUseCases.SesionAutenticada sesion = servicio.iniciarSesion(
                new CuentasUseCases.CredencialesInicioSesion(
                        "admin@example.com",
                        "secreto"
                )
        );

        assertThat(sesion.token()).isEqualTo("jwt-23");
        assertThat(sesion.perfil().tipoCuenta()).isEqualTo(CuentasUseCases.TipoCuenta.ADMIN);
        assertThat(sesion.perfil().nivelAcceso()).isEqualTo((short) 2);
    }

    @Test
    void permiteConsultarElPerfilAutenticadoDeUnaEntidad() {
        EntidadAliada entidad = new EntidadAliada(
                17L,
                "Fundación",
                "fundacion@example.com",
                "hash",
                OffsetDateTime.parse("2026-01-01T00:00:00Z"),
                "EDUCATIVA",
                "INST-17"
        );
        when(usuarios.buscarPorId(17L)).thenReturn(Optional.of(entidad));

        CuentasUseCases.Perfil perfil = servicio.obtenerMiPerfil(17L);

        assertThat(perfil.tipoCuenta()).isEqualTo(CuentasUseCases.TipoCuenta.ENTIDAD);
        assertThat(perfil.correo()).isEqualTo("fundacion@example.com");
    }

    @Test
    void conservaMensajeGenericoParaCredencialesIncorrectas() {
        when(usuarios.buscarPorCorreo("admin@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.iniciarSesion(
                new CuentasUseCases.CredencialesInicioSesion(
                        "admin@example.com",
                        "incorrecta"
                )
        ))
                .isInstanceOf(ErrorAplicacionException.class)
                .hasMessage("Correo o contraseña incorrectos");
    }

    private void prepararInicioSesion(Usuario usuario) {
        when(usuarios.buscarPorCorreo(usuario.getCorreo())).thenReturn(Optional.of(usuario));
        when(contrasenas.coincide("secreto", "hash")).thenReturn(true);
        when(tokens.generarTokenAcceso(usuario.getId())).thenReturn("jwt-" + usuario.getId());
    }
}
