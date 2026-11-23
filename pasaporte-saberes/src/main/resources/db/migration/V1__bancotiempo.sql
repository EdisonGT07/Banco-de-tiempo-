-- =====================================================================
-- Banco de Tiempo · Pasaporte de Saberes
-- V1__esquema_inicial.sql  (Flyway, PostgreSQL 15+)
-- Modelo lógico derivado del MER v4
--
-- Convenciones
--   * Nombres en español, snake_case, tablas en singular (como en el documento).
--   * Especialización de USUARIO: una tabla por subtipo que comparte la clave
--     (JPA: InheritanceType.JOINED). Total y disjunta, garantizada con una
--     clave foránea compuesta (usuario_id, tipo).
--   * Columnas marcadas "NO ESTÁ EN EL MER" son técnicas: agrégalas al MER
--     o quítalas de aquí, pero que ambos documentos coincidan.
--   * Reglas que se validan en el DOMINIO y no aquí:
--       - mayoría de edad (18 años) a partir de persona.fecha_nac
--       - el aprendiz de una sesión no puede ser el tutor de su oferta
--       - solo se califica una sesión en estado COMPLETADA
--       - el autor de un reporte participa en la sesión reportada
--   * [Decidir] Valores de modalidad y de umbrales: son propuestas.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Usuarios y especialización (total y disjunta)
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id               BIGSERIAL PRIMARY KEY,
    tipo             VARCHAR(10)  NOT NULL
                     CHECK (tipo IN ('PERSONA', 'ENTIDAD', 'ADMIN')),
    nombre           VARCHAR(120) NOT NULL,
    correo           VARCHAR(160) NOT NULL UNIQUE,
    contrasena_hash  VARCHAR(100) NOT NULL,
    habeas_data_en   TIMESTAMPTZ  NOT NULL,
    UNIQUE (id, tipo)
);

CREATE TABLE persona (
    usuario_id       BIGINT PRIMARY KEY,
    tipo             VARCHAR(10) NOT NULL DEFAULT 'PERSONA'
                     CHECK (tipo = 'PERSONA'),
    doc_hmac         CHAR(64)    NOT NULL UNIQUE,      -- HMAC-SHA256 del documento
    fecha_nac        DATE        NOT NULL,
    zona             VARCHAR(10) CHECK (zona IN ('URBANA', 'RURAL')),
    estrato          SMALLINT    CHECK (estrato BETWEEN 1 AND 6),
    estado           VARCHAR(12) NOT NULL DEFAULT 'ACTIVO'
                     CHECK (estado IN ('ACTIVO', 'SUSPENDIDO')),
    bloqueado_hasta  TIMESTAMPTZ,                      -- bloqueo temporal por inasistencia
    FOREIGN KEY (usuario_id, tipo) REFERENCES usuario (id, tipo)
);

CREATE TABLE entidad_aliada (
    usuario_id       BIGINT PRIMARY KEY,
    tipo             VARCHAR(10) NOT NULL DEFAULT 'ENTIDAD'
                     CHECK (tipo = 'ENTIDAD'),
    tipo_entidad     VARCHAR(40) NOT NULL,
    codigo_inst      VARCHAR(40) NOT NULL UNIQUE,
    FOREIGN KEY (usuario_id, tipo) REFERENCES usuario (id, tipo)
);

CREATE TABLE administrador (
    usuario_id       BIGINT PRIMARY KEY,
    tipo             VARCHAR(10) NOT NULL DEFAULT 'ADMIN'
                     CHECK (tipo = 'ADMIN'),
    nivel_acceso     SMALLINT    NOT NULL DEFAULT 1,
    FOREIGN KEY (usuario_id, tipo) REFERENCES usuario (id, tipo)
);

-- ---------------------------------------------------------------------
-- 2. Habilidades, intereses y ofertas
-- ---------------------------------------------------------------------
CREATE TABLE habilidad (
    id        BIGSERIAL PRIMARY KEY,
    nombre    VARCHAR(80) NOT NULL UNIQUE,
    categoria VARCHAR(60) NOT NULL
);

-- Relación N:M «Querer_aprender» (solicitud de aprendizaje, pestaña «Busco»)
CREATE TABLE querer_aprender (
    persona_id BIGINT NOT NULL REFERENCES persona (usuario_id),
    habilidad_id BIGINT NOT NULL REFERENCES habilidad (id),
    creada_en    TIMESTAMPTZ NOT NULL DEFAULT now(),    -- NO ESTÁ EN EL MER
    PRIMARY KEY (persona_id, habilidad_id)
);

CREATE TABLE oferta (
    id                 BIGSERIAL PRIMARY KEY,
    tutor_id           BIGINT        NOT NULL REFERENCES persona (usuario_id),
    habilidad_id       BIGINT        NOT NULL REFERENCES habilidad (id),
    descripcion        TEXT          NOT NULL,
    duracion_horas     NUMERIC(4,2)  NOT NULL CHECK (duracion_horas > 0),
    modalidad          VARCHAR(12)   NOT NULL
                       CHECK (modalidad IN ('PRESENCIAL', 'VIRTUAL')),   -- [Decidir]
    barrio             VARCHAR(80),
    destino_horas      VARCHAR(16)   NOT NULL DEFAULT 'BILLETERA_PROPIA'
                       CHECK (destino_horas IN ('BILLETERA_PROPIA', 'FONDO', 'REGALO')),
    receptor_regalo_id BIGINT        REFERENCES persona (usuario_id),  -- relación «Regalar_a»
    activa             BOOLEAN       NOT NULL DEFAULT TRUE,
    creada_en          TIMESTAMPTZ   NOT NULL DEFAULT now(),             -- NO ESTÁ EN EL MER
    CHECK ((destino_horas = 'REGALO') = (receptor_regalo_id IS NOT NULL)),
    CHECK (receptor_regalo_id IS DISTINCT FROM tutor_id)
);

-- ---------------------------------------------------------------------
-- 3. Sesión, evidencia, reseña y reporte
-- ---------------------------------------------------------------------
CREATE TABLE sesion (
    id                BIGSERIAL PRIMARY KEY,
    oferta_id         BIGINT      NOT NULL REFERENCES oferta (id),
    aprendiz_id       BIGINT      REFERENCES persona (usuario_id),     -- «Tomar» (0,1)
    inicia_en         TIMESTAMPTZ NOT NULL,
    lugar_o_enlace    VARCHAR(300),
    estado            VARCHAR(24) NOT NULL DEFAULT 'PROGRAMADA'
                      CHECK (estado IN ('PROGRAMADA', 'RESERVADA', 'EN_CURSO',
                                        'PENDIENTE_CONFIRMACION', 'COMPLETADA',
                                        'CANCELADA', 'NO_ASISTIO', 'EXPIRADA',
                                        'DISPUTADA')),
    fin_conf_tutor_en TIMESTAMPTZ,
    fin_conf_apr_en   TIMESTAMPTZ,
    inasistente       VARCHAR(10) CHECK (inasistente IN ('TUTOR', 'APRENDIZ')),
    version           BIGINT      NOT NULL DEFAULT 0,                     -- NO ESTÁ EN EL MER (bloqueo optimista)
    creada_en         TIMESTAMPTZ NOT NULL DEFAULT now(),                 -- NO ESTÁ EN EL MER
    CHECK (aprendiz_id IS NOT NULL OR estado IN ('PROGRAMADA', 'CANCELADA')),
    CHECK (estado <> 'NO_ASISTIO' OR inasistente IS NOT NULL),
    CHECK (estado <> 'COMPLETADA'
           OR (fin_conf_tutor_en IS NOT NULL AND fin_conf_apr_en IS NOT NULL))
);

CREATE TABLE evidencia (
    id                 BIGSERIAL PRIMARY KEY,
    sesion_id          BIGINT   NOT NULL UNIQUE REFERENCES sesion (id),  -- 1 a 1
    token_hash         CHAR(64) NOT NULL UNIQUE,
    codigo_corto_hash  CHAR(64) NOT NULL,
    expira_en          TIMESTAMPTZ NOT NULL,
    usado_en           TIMESTAMPTZ,
    intentos_fallidos  SMALLINT NOT NULL DEFAULT 0,                      -- NO ESTÁ EN EL MER (bloqueo a los 5)
    foto_url           TEXT,
    latitud            NUMERIC(9,6) CHECK (latitud  BETWEEN -90  AND 90),
    longitud           NUMERIC(9,6) CHECK (longitud BETWEEN -180 AND 180),
    marca_servidor     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE resena (
    id           BIGSERIAL PRIMARY KEY,
    sesion_id    BIGINT      NOT NULL REFERENCES sesion (id),
    autor_rol    VARCHAR(10) NOT NULL CHECK (autor_rol IN ('TUTOR', 'APRENDIZ')),
    calificacion SMALLINT    NOT NULL CHECK (calificacion BETWEEN 1 AND 5),
    comentario   TEXT,
    creada_en    TIMESTAMPTZ NOT NULL DEFAULT now(),                     -- NO ESTÁ EN EL MER
    UNIQUE (sesion_id, autor_rol)                                        -- máximo 2 por sesión: (0,2)
);

CREATE TABLE reporte (
    id          BIGSERIAL PRIMARY KEY,
    sesion_id   BIGINT      NOT NULL REFERENCES sesion (id),
    autor_id    BIGINT      NOT NULL REFERENCES persona (usuario_id),  -- «Presentar»
    revisor_id  BIGINT      REFERENCES administrador (usuario_id),       -- «Revisar» (0,1)
    motivo      TEXT        NOT NULL,
    estado      VARCHAR(12) NOT NULL DEFAULT 'ABIERTO'
                CHECK (estado IN ('ABIERTO', 'EN_REVISION', 'RESUELTO', 'DESCARTADO')),
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT now()                       -- NO ESTÁ EN EL MER
);

-- ---------------------------------------------------------------------
-- 4. Billetera y libro de transacciones
-- ---------------------------------------------------------------------
CREATE TABLE billetera (
    id               BIGSERIAL PRIMARY KEY,
    tipo             VARCHAR(10)  NOT NULL
                     CHECK (tipo IN ('USUARIO', 'FONDO', 'EMISOR')),
    persona_id     BIGINT       UNIQUE REFERENCES persona (usuario_id),   -- «Poseer»
    saldo_disponible NUMERIC(8,2) NOT NULL DEFAULT 0,
    saldo_reservado  NUMERIC(8,2) NOT NULL DEFAULT 0 CHECK (saldo_reservado >= 0),
    version          BIGINT       NOT NULL DEFAULT 0,                         -- NO ESTÁ EN EL MER (bloqueo optimista)
    CHECK ((tipo = 'USUARIO') = (persona_id IS NOT NULL)),
    CHECK (tipo = 'EMISOR' OR saldo_disponible >= 0)                          -- solo EMISOR puede quedar en negativo
);

-- Una sola billetera de sistema de cada tipo
CREATE UNIQUE INDEX uq_billetera_sistema ON billetera (tipo) WHERE tipo <> 'USUARIO';

INSERT INTO billetera (tipo) VALUES ('EMISOR'), ('FONDO');

CREATE TABLE transaccion (
    id                    BIGSERIAL PRIMARY KEY,
    tipo                  VARCHAR(14)  NOT NULL
                          CHECK (tipo IN ('BONO', 'RESERVA', 'LIBERACION', 'GANADA',
                                          'DONADA', 'EXCEDENTE', 'RETIRO_FONDO')),
    horas                 NUMERIC(8,2) NOT NULL CHECK (horas > 0),
    billetera_origen_id   BIGINT       NOT NULL REFERENCES billetera (id),    -- «Origen» (1,1)
    billetera_destino_id  BIGINT       NOT NULL REFERENCES billetera (id),    -- «Destino» (1,1)
    sesion_id             BIGINT       REFERENCES sesion (id),                -- «Respaldar» (0,1)
    clave_idemp           VARCHAR(80)  NOT NULL UNIQUE,
    creada_en             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CHECK (billetera_origen_id <> billetera_destino_id
           OR tipo IN ('RESERVA', 'LIBERACION')),
    -- Todo movimiento que nace de una clase queda ligado a su sesión. EXCEDENTE
    -- incluido: el tope se aplica al liquidar la sesión que hizo pasar el saldo del límite.
    CHECK (tipo NOT IN ('RESERVA', 'LIBERACION', 'GANADA', 'DONADA', 'EXCEDENTE')
           OR sesion_id IS NOT NULL)
);

-- El libro contable es de solo inserción: no se actualiza ni se borra.
CREATE FUNCTION bloquear_modificacion() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'La tabla % es de solo inserción', TG_TABLE_NAME;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER transaccion_solo_insercion
    BEFORE UPDATE OR DELETE ON transaccion
    FOR EACH ROW EXECUTE FUNCTION bloquear_modificacion();

-- ---------------------------------------------------------------------
-- 5. Pasaporte: credencial, aval y catálogo de rangos
-- ---------------------------------------------------------------------
CREATE TABLE credencial (
    id                 BIGSERIAL PRIMARY KEY,
    persona_id       BIGINT       NOT NULL REFERENCES persona (usuario_id),  -- «Obtener»
    habilidad_id       BIGINT       NOT NULL REFERENCES habilidad (id),          -- «Acreditar»
    horas_ensenadas    NUMERIC(8,2) NOT NULL DEFAULT 0 CHECK (horas_ensenadas >= 0),  -- derivado
    rating_promedio    NUMERIC(3,2) CHECK (rating_promedio BETWEEN 1 AND 5),          -- derivado
    codigo_verif       VARCHAR(32)  NOT NULL UNIQUE,
    firma              TEXT         NOT NULL,
    actualizada_en     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    num_verificaciones BIGINT       NOT NULL DEFAULT 0,                          -- NO ESTÁ EN EL MER (panel de impacto)
    UNIQUE (persona_id, habilidad_id)
);

CREATE TABLE aval (
    id            BIGSERIAL PRIMARY KEY,
    entidad_id    BIGINT      NOT NULL REFERENCES entidad_aliada (usuario_id),   -- «Emitir»
    credencial_id BIGINT      NOT NULL REFERENCES credencial (id),               -- «Avalar»
    fecha         DATE        NOT NULL DEFAULT CURRENT_DATE,
    vigencia_hasta DATE       NOT NULL,
    estado        VARCHAR(10) NOT NULL DEFAULT 'VIGENTE'
                  CHECK (estado IN ('VIGENTE', 'REVOCADO')),
    UNIQUE (entidad_id, credencial_id),
    CHECK (vigencia_hasta >= fecha)
);

-- Los rangos NO se guardan por persona: se calculan. Esta tabla es el catálogo de umbrales.
CREATE TABLE rango (
    id             BIGSERIAL PRIMARY KEY,
    escala         VARCHAR(12)  NOT NULL CHECK (escala IN ('ENSENANZA', 'APRENDIZAJE')),
    nombre         VARCHAR(30)  NOT NULL,
    orden          SMALLINT     NOT NULL,
    min_horas      NUMERIC(8,2) NOT NULL,
    min_rating     NUMERIC(3,2),
    min_aprendices INTEGER,
    min_habilidades INTEGER,
    min_asistencia NUMERIC(4,3),
    UNIQUE (escala, nombre),
    UNIQUE (escala, orden)
);

INSERT INTO rango (escala, nombre, orden, min_horas, min_rating, min_aprendices, min_habilidades, min_asistencia) VALUES
    ('ENSENANZA',   'Pedagogo',  1,   1, NULL, NULL, NULL, NULL),
    ('ENSENANZA',   'Mentor',    2,  10, 4.0,    5, NULL, NULL),
    ('ENSENANZA',   'Maestro',   3,  50, 4.2,   15, NULL, NULL),
    ('ENSENANZA',   'Filósofo',  4, 150, 4.4,   40, NULL, NULL),
    ('ENSENANZA',   'Sabio',     5, 500, 4.5,  100, NULL, NULL),
    ('APRENDIZAJE', 'Oyente',    1,   1, NULL, NULL, NULL, NULL),
    ('APRENDIZAJE', 'Discípulo', 2,  10, NULL, NULL,    2, NULL),
    ('APRENDIZAJE', 'Estudioso', 3,  40, NULL, NULL,    4, 0.900),
    ('APRENDIZAJE', 'Erudito',   4, 100, NULL, NULL,    6, 0.900),
    ('APRENDIZAJE', 'Polímata',  5, 250, NULL, NULL,   10, 0.900);

-- «Tener_rango_ens» y «Tener_rango_apr» (MER v4): rango ACTUAL de cada escala.
-- Es una caché de un valor calculado: solo la actualiza el caso de uso CalcularRangos
-- al completarse una sesión; nadie lo edita a mano. NULL = todavía sin rango.
-- Regla de dominio: rango_ens_id apunta a un rango de escala ENSENANZA y
-- rango_apr_id a uno de escala APRENDIZAJE.
ALTER TABLE persona
    ADD COLUMN rango_ens_id BIGINT REFERENCES rango (id),
    ADD COLUMN rango_apr_id BIGINT REFERENCES rango (id);

-- ---------------------------------------------------------------------
-- 6. Índices para las consultas frecuentes
-- ---------------------------------------------------------------------
CREATE INDEX ix_oferta_habilidad     ON oferta (habilidad_id) WHERE activa;
CREATE INDEX ix_oferta_tutor         ON oferta (tutor_id);
CREATE INDEX ix_oferta_texto         ON oferta USING GIN (to_tsvector('spanish', descripcion));
CREATE INDEX ix_sesion_oferta        ON sesion (oferta_id);
CREATE INDEX ix_sesion_aprendiz      ON sesion (aprendiz_id);
CREATE INDEX ix_sesion_estado_inicio ON sesion (estado, inicia_en);       -- tareas programadas
CREATE INDEX ix_transaccion_origen   ON transaccion (billetera_origen_id);
CREATE INDEX ix_transaccion_destino  ON transaccion (billetera_destino_id);
CREATE INDEX ix_transaccion_sesion   ON transaccion (sesion_id);
CREATE INDEX ix_credencial_habilidad ON credencial (habilidad_id);
CREATE INDEX ix_persona_rango_ens    ON persona (rango_ens_id);
CREATE INDEX ix_persona_rango_apr    ON persona (rango_apr_id);
CREATE INDEX ix_reporte_estado       ON reporte (estado);