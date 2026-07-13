CREATE DATABASE IF NOT EXISTS comunigest_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE comunigest_db;

CREATE TABLE IF NOT EXISTS perfiles (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(80) NOT NULL,
  descripcion VARCHAR(255) NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  UNIQUE KEY uk_perfiles_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS departamentos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  torre VARCHAR(40) NOT NULL,
  numero VARCHAR(40) NOT NULL,
  piso SMALLINT NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'HABITADO',
  observaciones VARCHAR(500) NULL,
  creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_departamentos_torre_numero (torre, numero)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS usuarios (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(120) NOT NULL,
  email VARCHAR(160) NOT NULL,
  password VARCHAR(255) NOT NULL,
  perfil_id BIGINT NOT NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  UNIQUE KEY uk_usuarios_email (email),
  KEY idx_usuarios_perfil_id (perfil_id),
  CONSTRAINT fk_usuarios_perfiles
    FOREIGN KEY (perfil_id) REFERENCES perfiles (id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS residentes (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nombres VARCHAR(100) NOT NULL,
  apellidos VARCHAR(100) NOT NULL,
  rut VARCHAR(20) NULL,
  telefono VARCHAR(30) NULL,
  email VARCHAR(160) NULL,
  tipo_residente VARCHAR(20) NOT NULL DEFAULT 'PROPIETARIO',
  departamento_id BIGINT NOT NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_residentes_departamento_id (departamento_id),
  CONSTRAINT fk_residentes_departamentos
    FOREIGN KEY (departamento_id) REFERENCES departamentos (id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS turnos (
  id BIGINT NOT NULL AUTO_INCREMENT,
  usuario_id BIGINT NOT NULL,
  fecha_inicio DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  fecha_cierre DATETIME(6) NULL,
  observaciones_inicio VARCHAR(500) NULL,
  observaciones_cierre VARCHAR(500) NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTO',
  creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_turnos_usuario_id (usuario_id),
  KEY idx_turnos_estado (estado),
  CONSTRAINT chk_turnos_estado CHECK (estado IN ('ABIERTO', 'CERRADO')),
  CONSTRAINT fk_turnos_usuarios
    FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS visitas (
  id BIGINT NOT NULL AUTO_INCREMENT,
  nombre_visitante VARCHAR(160) NOT NULL,
  documento_identidad VARCHAR(40) NOT NULL,
  patente VARCHAR(20) NULL,
  motivo VARCHAR(255) NULL,
  departamento_id BIGINT NOT NULL,
  residente_autorizador_id BIGINT NULL,
  turno_ingreso_id BIGINT NULL,
  turno_salida_id BIGINT NULL,
  fecha_ingreso DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  fecha_salida DATETIME(6) NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'INGRESADA',
  observaciones VARCHAR(500) NULL,
  creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_visitas_departamento_id (departamento_id),
  KEY idx_visitas_residente_autorizador_id (residente_autorizador_id),
  KEY idx_visitas_turno_ingreso_id (turno_ingreso_id),
  KEY idx_visitas_turno_salida_id (turno_salida_id),
  KEY idx_visitas_estado (estado),
  CONSTRAINT chk_visitas_estado CHECK (estado IN ('INGRESADA', 'SALIDA')),
  CONSTRAINT fk_visitas_departamentos
    FOREIGN KEY (departamento_id) REFERENCES departamentos (id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT fk_visitas_residentes
    FOREIGN KEY (residente_autorizador_id) REFERENCES residentes (id)
    ON UPDATE CASCADE
    ON DELETE SET NULL,
  CONSTRAINT fk_visitas_turno_ingreso
    FOREIGN KEY (turno_ingreso_id) REFERENCES turnos (id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT fk_visitas_turno_salida
    FOREIGN KEY (turno_salida_id) REFERENCES turnos (id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS encomiendas (
  id BIGINT NOT NULL AUTO_INCREMENT,
  destinatario VARCHAR(160) NOT NULL,
  descripcion VARCHAR(255) NOT NULL,
  empresa_repartidor VARCHAR(160) NULL,
  codigo_recepcion VARCHAR(80) NULL,
  recibido_por VARCHAR(140) NULL,
  entregado_a VARCHAR(140) NULL,
  departamento_id BIGINT NOT NULL,
  turno_recepcion_id BIGINT NULL,
  turno_entrega_id BIGINT NULL,
  fecha_recepcion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  fecha_entrega DATETIME(6) NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
  PRIMARY KEY (id),
  KEY idx_encomiendas_departamento_id (departamento_id),
  KEY idx_encomiendas_turno_recepcion_id (turno_recepcion_id),
  KEY idx_encomiendas_turno_entrega_id (turno_entrega_id),
  KEY idx_encomiendas_estado (estado),
  CONSTRAINT chk_encomiendas_estado CHECK (estado IN ('PENDIENTE', 'ENTREGADA')),
  CONSTRAINT fk_encomiendas_departamentos
    FOREIGN KEY (departamento_id) REFERENCES departamentos (id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT fk_encomiendas_turno_recepcion
    FOREIGN KEY (turno_recepcion_id) REFERENCES turnos (id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT fk_encomiendas_turno_entrega
    FOREIGN KEY (turno_entrega_id) REFERENCES turnos (id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS incidencias (
  id BIGINT NOT NULL AUTO_INCREMENT,
  titulo VARCHAR(140) NOT NULL,
  descripcion VARCHAR(1000) NOT NULL,
  categoria VARCHAR(80) NULL,
  criticidad VARCHAR(40) NOT NULL DEFAULT 'MEDIA',
  registrada_por_id BIGINT NULL,
  turno_registro_id BIGINT NULL,
  fecha_registro DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  fecha_resolucion DATETIME(6) NULL,
  usuario_resuelve_id BIGINT NULL,
  resolucion VARCHAR(1000) NULL,
  estado VARCHAR(20) NOT NULL DEFAULT 'ABIERTA',
  creado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_incidencias_registrada_por_id (registrada_por_id),
  KEY idx_incidencias_turno_registro_id (turno_registro_id),
  KEY idx_incidencias_usuario_resuelve_id (usuario_resuelve_id),
  KEY idx_incidencias_estado (estado),
  CONSTRAINT chk_incidencias_estado CHECK (estado IN ('ABIERTA', 'EN_PROCESO', 'RESUELTA')),
  CONSTRAINT fk_incidencias_usuarios
    FOREIGN KEY (registrada_por_id) REFERENCES usuarios (id)
    ON UPDATE CASCADE
    ON DELETE SET NULL,
  CONSTRAINT fk_incidencias_turno_registro
    FOREIGN KEY (turno_registro_id) REFERENCES turnos (id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,
  CONSTRAINT fk_incidencias_usuario_resuelve
    FOREIGN KEY (usuario_resuelve_id) REFERENCES usuarios (id)
    ON UPDATE CASCADE
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO perfiles (nombre, descripcion, activo)
VALUES
  ('ADMINISTRADOR', 'Administración general del sistema', 1),
  ('CONSERJE', 'Operación diaria de conserjería', 1)
ON DUPLICATE KEY UPDATE
  descripcion = CASE nombre
    WHEN 'ADMINISTRADOR' THEN 'Administración general del sistema'
    WHEN 'CONSERJE' THEN 'Operación diaria de conserjería'
    ELSE descripcion
  END,
  activo = 1;

INSERT INTO usuarios (nombre, email, password, perfil_id, activo)
SELECT
  'Administrador ComuniGest',
  'admin@comunigest.local',
  '$2a$10$4T4hPeieESBFRKEx2ESzRe6PHzW4yLj9Qj6mYg.57MfYO8hlNltfy',
  p.id,
  1
FROM perfiles p
WHERE p.nombre = 'ADMINISTRADOR'
ON DUPLICATE KEY UPDATE
  nombre = 'Administrador ComuniGest',
  perfil_id = (SELECT id FROM perfiles WHERE nombre = 'ADMINISTRADOR'),
  activo = 1;
