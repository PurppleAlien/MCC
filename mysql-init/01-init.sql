-- Crear base de datos del catálogo si no existe
CREATE DATABASE IF NOT EXISTS uamishop_catalogo
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Garantizar permisos al usuario uamishop en ambas BDs
GRANT ALL PRIVILEGES ON uamishop.* TO 'uamishop'@'%';
GRANT ALL PRIVILEGES ON uamishop_catalogo.* TO 'uamishop'@'%';

FLUSH PRIVILEGES;
