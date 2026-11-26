-- ============================================================================
-- UNIVERSIDAD TÉCNICA NACIONAL - SEDE DE GUANACASTE
-- CURSO: PROGRAMACIÓN II - PROYECTO II
-- SCRIPT DE BASE DE DATOS: BeneficiosUTN-DB
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `BeneficiosUTN-DB`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE `BeneficiosUTN-DB`;

-- ----------------------------------------------------------------------------
-- 1. Tabla: Carreras
-- Almacena la información referente a las carreras que se ofrecen en la UTN.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `Carreras` (
    `idCarrera` INT NOT NULL,
    `nomCarrera` VARCHAR(120) NOT NULL,
    `grado` VARCHAR(45) NOT NULL COMMENT 'Diplomado, Bachiller, Maestría',
    PRIMARY KEY (`idCarrera`)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 2. Tabla: Beneficios
-- Almacena la información referente a todos los beneficios complementarios.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `Beneficios` (
    `idBeneficio` INT NOT NULL,
    `nomBeneficio` VARCHAR(100) NOT NULL,
    `descripcion` VARCHAR(255) NOT NULL,
    `montoBeneficio` DECIMAL(10, 2) NOT NULL COMMENT 'Monto en colones con 2 decimales',
    PRIMARY KEY (`idBeneficio`)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 3. Tabla: Estudiante
-- Almacena la información de los estudiantes (hereda los atributos de Persona).
-- - carnet es PK (único, no permite duplicados)
-- - cedula es UNIQUE (no permite duplicados y es referenciada por otras tablas)
-- - email es UNIQUE (único en la tabla)
-- - fechEgreso permite NULL (puede quedar vacío)
-- - idCarrera es FK hacia Carreras(idCarrera)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `Estudiante` (
    `carnet` VARCHAR(30) NOT NULL,
    `cedula` INT NOT NULL,
    `nombre` VARCHAR(120) NOT NULL COMMENT 'Incluye apellidos',
    `fechNac` DATE NOT NULL,
    `direccion` VARCHAR(200) NOT NULL,
    `telefono` VARCHAR(25) NOT NULL,
    `email` VARCHAR(100) NOT NULL,
    `fechIngreso` DATE NOT NULL,
    `fechEgreso` DATE NULL DEFAULT NULL COMMENT 'Puede quedar vacío',
    `idCarrera` INT NOT NULL,
    PRIMARY KEY (`carnet`),
    UNIQUE INDEX `uk_estudiante_cedula` (`cedula`),
    UNIQUE INDEX `uk_estudiante_email` (`email`),
    INDEX `idx_estudiante_carrera` (`idCarrera`),
    CONSTRAINT `fk_estudiante_carreras`
        FOREIGN KEY (`idCarrera`)
        REFERENCES `Carreras` (`idCarrera`)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 4. Tabla: BeneficiosEstudiantes (Tabla Intermedia)
-- Almacena los beneficios asignados a cada estudiante.
-- Tanto cedula como idBeneficio son llaves foráneas y pueden repetirse
-- (un estudiante puede tener varios beneficios asignados).
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `BeneficiosEstudiantes` (
    `idBeneficioEstud` INT NOT NULL AUTO_INCREMENT,
    `cedula` INT NOT NULL,
    `idBeneficio` INT NOT NULL,
    PRIMARY KEY (`idBeneficioEstud`),
    INDEX `idx_benef_estud_cedula` (`cedula`),
    INDEX `idx_benef_estud_beneficio` (`idBeneficio`),
    CONSTRAINT `fk_benef_estud_estudiante`
        FOREIGN KEY (`cedula`)
        REFERENCES `Estudiante` (`cedula`)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT `fk_benef_estud_beneficios`
        FOREIGN KEY (`idBeneficio`)
        REFERENCES `Beneficios` (`idBeneficio`)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- 5. Tabla: PagosMensuales
-- Registro de todos los pagos mensuales efectuados a los estudiantes.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `PagosMensuales` (
    `idPago` INT NOT NULL AUTO_INCREMENT,
    `fechCreacion` DATE NOT NULL,
    `mes` VARCHAR(30) NOT NULL COMMENT 'Mes en texto, ej: Julio o Julio 2026',
    `fechPago` DATE NOT NULL,
    `estudiante` INT NOT NULL COMMENT 'Cédula del estudiante (FK)',
    `totalBeneficios` DECIMAL(10, 2) NOT NULL,
    `deducSeguro` DECIMAL(10, 2) NOT NULL COMMENT '10% de totalBeneficios',
    `deducRenta` DECIMAL(10, 2) NOT NULL COMMENT '5% de totalBeneficios',
    `pagoNeto` DECIMAL(10, 2) NOT NULL COMMENT 'totalBeneficios - (deducSeguro + deducRenta)',
    PRIMARY KEY (`idPago`),
    INDEX `idx_pagos_estudiante` (`estudiante`),
    CONSTRAINT `fk_pagos_estudiante`
        FOREIGN KEY (`estudiante`)
        REFERENCES `Estudiante` (`cedula`)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB;
