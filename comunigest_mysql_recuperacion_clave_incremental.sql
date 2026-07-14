SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

USE comunigest_db;

CREATE TABLE IF NOT EXISTS tokens_recuperacion_clave (
  id BIGINT NOT NULL AUTO_INCREMENT,
  hash_token VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  usuario_id BIGINT NOT NULL,
  fecha_creacion DATETIME(6) NOT NULL,
  fecha_expiracion DATETIME(6) NOT NULL,
  fecha_uso DATETIME(6) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_tokens_recuperacion_hash (hash_token),
  KEY idx_tokens_recuperacion_usuario_id (usuario_id),
  KEY idx_tokens_recuperacion_expiracion (fecha_expiracion),
  CONSTRAINT fk_tokens_recuperacion_usuarios
    FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
