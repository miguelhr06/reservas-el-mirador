-- ==========================================================
-- SCRIPT DE BASE DE DATOS: EL MIRADOR DE SANTA EULALIA
-- FECHA: 2025
-- MOTOR: MySQL / MariaDB
-- DESCRIPCIÓN: Estructura relacional optimizada para producción.
-- ==========================================================
create database el_mirador_db;
drop database EL_MIRADOR_DE_SANTA_EULALIA; 
use el_mirador_db;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------
-- 1. TABLA ROLES
-- Define los 3 roles estrictos del sistema [cite: 197]
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
  `id_rol` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(50) NOT NULL,
  `descripcion` VARCHAR(255) NULL,
  PRIMARY KEY (`id_rol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insertar roles predefinidos
INSERT INTO `roles` (`id_rol`, `nombre`, `descripcion`) VALUES
(1, 'CLIENTE', 'Usuario final que realiza reservas via web'),
(2, 'RECEPCION', 'Empleado que gestiona check-in, check-out y reservas presenciales'),
(3, 'ADMINISTRADOR', 'Acceso total a reportes, gestión de usuarios y configuración');

-- ----------------------------------------------------------
-- 2. TABLA USUARIOS
-- Centraliza clientes y empleados con seguridad [cite: 163, 195]
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `usuarios`;
CREATE TABLE `usuarios` (
  `id_usuario` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(100) NOT NULL,
  `apellido` VARCHAR(100) NOT NULL,
  `email` VARCHAR(150) NOT NULL UNIQUE, -- Login principal
  `password_hash` VARCHAR(255) NOT NULL, -- Contraseña encriptada (Bcrypt/Argon2)
  `telefono` VARCHAR(20) NULL,
  `tipo_documento` ENUM('DNI', 'CE', 'PASAPORTE', 'RUC') NOT NULL DEFAULT 'DNI',
  `numero_documento` VARCHAR(20) NOT NULL,
  `id_rol` INT NOT NULL,
  `estado` TINYINT(1) DEFAULT 1, -- 1: Activo, 0: Inactivo (Baneado/Despedido)
  `fecha_registro` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_usuario`),
  CONSTRAINT `fk_usuarios_rol` FOREIGN KEY (`id_rol`) REFERENCES `roles` (`id_rol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------
-- 3. TABLA TIPOS DE RECURSO
-- Categorías de reserva: Bungalows, Mesas, Espacios [cite: 192]
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `tipos_recurso`;
CREATE TABLE `tipos_recurso` (
  `id_tipo` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(50) NOT NULL, -- 'BUNGALOW', 'MESA', 'ESPACIO'
  PRIMARY KEY (`id_tipo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `tipos_recurso` (`id_tipo`, `nombre`) VALUES
(1, 'BUNGALOW'),
(2, 'MESA'),
(3, 'ESPACIO RECREATIVO');

-- ----------------------------------------------------------
-- 4. TABLA RECURSOS (INVENTARIO)
-- Los items físicos reales que se reservan [cite: 162]
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `recursos`;
CREATE TABLE `recursos` (
  `id_recurso` INT NOT NULL AUTO_INCREMENT,
  `nombre` VARCHAR(100) NOT NULL, -- Ej: 'Bungalow Matrimonial 01'
  `descripcion` TEXT NULL,
  `capacidad` INT NOT NULL DEFAULT 1, -- Aforo máximo para reportes
  `precio_base` DECIMAL(10,2) NOT NULL, -- Precio por Noche (Bungalow) o por Día (Mesa)
  `imagen_url` VARCHAR(255) NULL,
  `id_tipo` INT NOT NULL,
  `estado` ENUM('DISPONIBLE', 'MANTENIMIENTO', 'FUERA_DE_SERVICIO') DEFAULT 'DISPONIBLE',
  PRIMARY KEY (`id_recurso`),
  CONSTRAINT `fk_recursos_tipo` FOREIGN KEY (`id_tipo`) REFERENCES `tipos_recurso` (`id_tipo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------
-- 5. TABLA RESERVAS (CORE)
-- Maneja la disponibilidad y el estado del servicio [cite: 161, 162]
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `reservas`;
CREATE TABLE `reservas` (
  `id_reserva` INT NOT NULL AUTO_INCREMENT,
  `codigo_reserva` VARCHAR(20) NOT NULL UNIQUE, -- Código amigable Ej: 'RES-9021'
  `id_usuario` INT NOT NULL, -- Cliente que reserva
  `id_recurso` INT NOT NULL, -- Qué está reservando
  `fecha_inicio` DATETIME NOT NULL, -- Entrada (Check-in)
  `fecha_fin` DATETIME NOT NULL, -- Salida (Check-out)
  `cantidad_personas` INT NOT NULL,
  `precio_total` DECIMAL(10,2) NOT NULL, -- Precio pactado al momento de reservar
  `estado` ENUM('PENDIENTE_PAGO', 'CONFIRMADA', 'COMPLETADA', 'CANCELADA', 'NO_SHOW') DEFAULT 'PENDIENTE_PAGO',
  `origen` ENUM('WEB', 'PRESENCIAL') DEFAULT 'WEB', -- Para estadísticas de ventas
  `observaciones` TEXT NULL, -- Notas especiales del cliente
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_reserva`),
  KEY `idx_fechas` (`fecha_inicio`, `fecha_fin`), -- Índice CRÍTICO para buscar disponibilidad rápida
  CONSTRAINT `fk_reservas_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`),
  CONSTRAINT `fk_reservas_recurso` FOREIGN KEY (`id_recurso`) REFERENCES `recursos` (`id_recurso`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------
-- 6. TABLA PAGOS
-- Soporta pagos parciales y múltiples métodos [cite: 164, 202]
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `pagos`;
CREATE TABLE `pagos` (
  `id_pago` INT NOT NULL AUTO_INCREMENT,
  `id_reserva` INT NOT NULL,
  `monto` DECIMAL(10,2) NOT NULL,
  `metodo_pago` ENUM('YAPE', 'PLIN', 'TARJETA', 'EFECTIVO', 'TRANSFERENCIA') NOT NULL,
  `codigo_operacion` VARCHAR(50) NULL, -- Número de operación de Yape/Plin
  `comprobante_img` VARCHAR(255) NULL, -- URL de la captura de pantalla
  `fecha_pago` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `estado` ENUM('PENDIENTE', 'APROBADO', 'RECHAZADO') DEFAULT 'PENDIENTE', -- Para validación manual si es necesario
  PRIMARY KEY (`id_pago`),
  CONSTRAINT `fk_pagos_reserva` FOREIGN KEY (`id_reserva`) REFERENCES `reservas` (`id_reserva`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------
-- 7. TABLA BLOQUEOS DE CALENDARIO
-- Para mantenimiento sin crear reservas falsas [cite: 200]
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `bloqueos_calendario`;
CREATE TABLE `bloqueos_calendario` (
  `id_bloqueo` INT NOT NULL AUTO_INCREMENT,
  `id_recurso` INT NOT NULL,
  `fecha_inicio` DATETIME NOT NULL,
  `fecha_fin` DATETIME NOT NULL,
  `motivo` VARCHAR(255) NOT NULL, -- Ej: 'Reparación de tuberías'
  PRIMARY KEY (`id_bloqueo`),
  CONSTRAINT `fk_bloqueos_recurso` FOREIGN KEY (`id_recurso`) REFERENCES `recursos` (`id_recurso`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================================
-- DATOS DE PRUEBA (SEEDING)
-- Para que el sistema no esté vacío al iniciar
-- ==========================================================

-- 1. Usuarios (Password es '123456' en hash bcrypt referencial)
INSERT INTO `usuarios` (`nombre`, `apellido`, `email`, `password_hash`, `numero_documento`, `id_rol`) VALUES
('Juan', 'Admin', 'admin@elmirador.com', '$2a$12$DUMMYHASHFOR123456', '70000001', 3),
('Maria', 'Recepcionista', 'recepcion@elmirador.com', '$2a$12$DUMMYHASHFOR123456', '70000002', 2),
('Carlos', 'Cliente', 'cliente@gmail.com', '$2a$12$DUMMYHASHFOR123456', '40000001', 1);

-- 2. Inventario
INSERT INTO `recursos` (`nombre`, `descripcion`, `capacidad`, `precio_base`, `id_tipo`) VALUES
('Bungalow Matrimonial 101', 'Vista al río, cama King, baño privado', 2, 150.00, 1),
('Bungalow Familiar 102', 'Dos habitaciones, sala, kitchenette', 5, 280.00, 1),
('Mesa Zona Piscina 05', 'Mesa con sombrilla cerca a la piscina', 4, 20.00, 2),
('Cancha de Fulbito', 'Cancha de grass natural por hora', 12, 50.00, 3);

-- Asume que tu usuario tiene id_usuario = 1 (o el que sea)
-- Rol 3 = ADMINISTRADOR (según nuestro script inicial)
UPDATE usuarios SET id_rol = 3 WHERE email = 'santiagogeldrespiero@gmail.com';


-- ============================================================
-- SCRIPT DE POBLADO DE RECURSOS - EL MIRADOR DE SANTA EULALIA
-- Estimación basada en infraestructura de turismo rural
-- Incluye inicialización de 'version' para JPA
-- ============================================================

INSERT INTO `recursos` (`nombre`, `descripcion`, `capacidad`, `precio_base`, `id_tipo`, `version`, `estado`) VALUES

-- ------------------------------------------------------------
-- TIPO 1: BUNGALOWS (HOSPEDAJE)
-- ------------------------------------------------------------
-- Bungalows Matrimoniales (Parejas)
('Bungalow Matrimonial 103', 'Vista panorámica al valle, cama Queen, TV Cable', 2, 160.00, 1, 0, 'DISPONIBLE'),
('Bungalow Matrimonial 104', 'Cerca a la orilla del río, terraza privada, frigobar', 2, 180.00, 1, 0, 'DISPONIBLE'),
('Bungalow Matrimonial 105', 'Ubicación tranquila zona alta, cama King', 2, 150.00, 1, 0, 'DISPONIBLE'),

-- Bungalows Familiares (Pequeños - 4 personas)
('Bungalow Familiar 106', 'Una habitación con camarotes, sala de estar', 4, 250.00, 1, 0, 'DISPONIBLE'),
('Bungalow Familiar 107', 'Vista a la piscina, kitchenette equipado', 4, 260.00, 1, 0, 'DISPONIBLE'),

-- Bungalows Familiares (Grandes - 6 a 8 personas)
('Bungalow Familiar 201', 'Dúplex rústico, dos baños, balcón amplio', 6, 350.00, 1, 0, 'DISPONIBLE'),
('Bungalow Familiar 202', 'Especial para grupos, zona de parrilla privada', 8, 420.00, 1, 0, 'DISPONIBLE'),
('Bungalow Premium 203', 'La mejor vista del club, acabados de lujo, jacuzzi', 6, 500.00, 1, 0, 'DISPONIBLE'),


-- ------------------------------------------------------------
-- TIPO 2: MESAS (RESTAURANTE Y ZONAS DE ESTAR)
-- Nota: Precio base puede ser un "derecho de reserva" o alquiler de Box
-- ------------------------------------------------------------
-- Zona Río (La más solicitada)
('Mesa Zona Río 01', 'Mesa de madera junto al río Santa Eulalia', 6, 30.00, 2, 0, 'DISPONIBLE'),
('Mesa Zona Río 02', 'Mesa de madera junto al río Santa Eulalia', 6, 30.00, 2, 0, 'DISPONIBLE'),
('Mesa Zona Río 03', 'Ubicación preferencial bajo sombra natural', 8, 35.00, 2, 0, 'DISPONIBLE'),
('Mesa Zona Río 04', 'Ubicación preferencial bajo sombra natural', 8, 35.00, 2, 0, 'DISPONIBLE'),

-- Zona Piscina (Tipo Box)
('Box Piscina A', 'Lounge privado frente a la piscina, incluye sombrilla grande', 8, 50.00, 2, 0, 'DISPONIBLE'),
('Box Piscina B', 'Lounge privado frente a la piscina, incluye sombrilla grande', 8, 50.00, 2, 0, 'DISPONIBLE'),
('Mesa Familiar Piscina 01', 'Mesa redonda familiar cerca a patera de niños', 5, 25.00, 2, 0, 'DISPONIBLE'),
('Mesa Familiar Piscina 02', 'Mesa redonda familiar cerca a patera de niños', 5, 25.00, 2, 0, 'DISPONIBLE'),

-- Zona Jardín (General)
('Mesa Campestre 10', 'Zona de gras, ideal para picnic familiar', 6, 15.00, 2, 0, 'DISPONIBLE'),
('Mesa Campestre 11', 'Zona de gras, ideal para picnic familiar', 6, 15.00, 2, 0, 'DISPONIBLE'),
('Mesa Campestre 12', 'Cerca a los juegos infantiles', 4, 15.00, 2, 0, 'DISPONIBLE'),
('Mesa Campestre 13', 'Cerca a los juegos infantiles', 4, 15.00, 2, 0, 'DISPONIBLE'),
('Mesa Grande Eventos', 'Tablón largo para celebraciones o cumpleaños', 15, 80.00, 2, 0, 'DISPONIBLE'),


-- ------------------------------------------------------------
-- TIPO 3: ESPACIOS RECREATIVOS
-- ------------------------------------------------------------
-- Zonas de Parrilla (Se alquilan por turno/día)
('Zona de Parrilla 01', 'Parrilla de ladrillo, lavadero y mesa auxiliar', 10, 40.00, 3, 0, 'DISPONIBLE'),
('Zona de Parrilla 02', 'Parrilla de ladrillo, lavadero y mesa auxiliar', 10, 40.00, 3, 0, 'DISPONIBLE'),
('Zona de Parrilla 03', 'Parrilla amplia techada', 12, 50.00, 3, 0, 'DISPONIBLE'),

-- Deportes
('Cancha de Voley', 'Cancha de césped para voley o bádminton (por hora)', 12, 30.00, 3, 0, 'DISPONIBLE'),
('Loza Deportiva Multi', 'Cemento, para fulbito o basket (por hora)', 14, 40.00, 3, 0, 'DISPONIBLE');

ALTER TABLE reservas 
MODIFY COLUMN estado ENUM('PENDIENTE_PAGO', 'ESPERANDO_CONFIRMACION', 'CONFIRMADA', 'COMPLETADA', 'CANCELADA', 'NO_SHOW') 
DEFAULT 'PENDIENTE_PAGO';