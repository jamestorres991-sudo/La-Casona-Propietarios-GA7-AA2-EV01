-- EVIDENCIA GA7-220501096-AA2-EV01. Base de datos para el módulo de propietarios.
-- Clínica Veterinaria La Casona
-- Aprendiz: James Alberto Torres Peláez | Ficha: 3186594
-- Programa: Análisis y Desarrollo de Software | SENA
--
-- Se conserva el modelo físico de GA6-220501096-AA2-EV03.
-- Las tablas y vistas se exportaron del modelo Clinica_La_Casona.mwb
-- mediante MySQL Workbench.
-- contiene: 19 tablas, 95 atributos, 19 llaves primarias,
-- 23 llaves foráneas, 8 reglas de valores únicos, 22 CHECK y 2 vistas.
--
-- La base nueva se llama la_casona_ev01_pruebas.
-- Las llaves y los CHECK protegen relaciones y valores del modelo.
-- Se utiliza utf8mb4 para que la conexión interprete correctamente
-- las tildes, la letra ñ y otros caracteres especiales.

-- 1. CREACIÓN DE LA BASE

CREATE SCHEMA `la_casona_ev01_pruebas` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

USE `la_casona_ev01_pruebas`;
SET NAMES utf8mb4;

-- 2. TABLAS, ATRIBUTOS, LLAVES E ÍNDICES
-- Se crean primero las tablas de las que dependen las demás.

-- Tabla: ROL

CREATE TABLE `la_casona_ev01_pruebas`.`ROL` (
  `id_rol` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Identificador del rol',
  `nombre_rol` VARCHAR(30) NOT NULL COMMENT 'Nombre del rol',
  PRIMARY KEY (`id_rol`),
  UNIQUE INDEX `uq_rol_1` (`nombre_rol` ASC) VISIBLE)
ENGINE = InnoDB
COMMENT = 'Identifica las funciones de acceso del personal.';

-- Tabla: USUARIO

CREATE TABLE `la_casona_ev01_pruebas`.`USUARIO` (
  `id_usuario` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Identificador del usuario',
  `id_rol` INT UNSIGNED NOT NULL COMMENT 'Rol asignado',
  `nombre_usuario` VARCHAR(50) NOT NULL COMMENT 'Nombre usado para ingresar',
  `clave_hash` VARCHAR(255) NOT NULL COMMENT 'Contraseña transformada por la aplicación',
  `activo` TINYINT NOT NULL DEFAULT 1 COMMENT '1 activo y 0 inactivo',
  PRIMARY KEY (`id_usuario`),
  UNIQUE INDEX `uq_usuario_1` (`nombre_usuario` ASC) VISIBLE,
  INDEX `fk_usuario_id_rol` (`id_rol` ASC) VISIBLE,
  CONSTRAINT `fk_usuario_id_rol`
    FOREIGN KEY (`id_rol`)
    REFERENCES `la_casona_ev01_pruebas`.`ROL` (`id_rol`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
COMMENT = 'Conserva la cuenta del personal y su rol.';

-- Tabla: VETERINARIO

CREATE TABLE `la_casona_ev01_pruebas`.`VETERINARIO` (
  `id_veterinario` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Identificador del profesional',
  `id_usuario` INT UNSIGNED NULL DEFAULT NULL COMMENT 'Cuenta opcional del profesional',
  `nombres` VARCHAR(80) NOT NULL COMMENT 'Nombres del profesional',
  `apellidos` VARCHAR(80) NOT NULL COMMENT 'Apellidos del profesional',
  PRIMARY KEY (`id_veterinario`),
  UNIQUE INDEX `uq_veterinario_1` (`id_usuario` ASC) VISIBLE,
  CONSTRAINT `fk_veterinario_id_usuario`
    FOREIGN KEY (`id_usuario`)
    REFERENCES `la_casona_ev01_pruebas`.`USUARIO` (`id_usuario`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
COMMENT = 'Registra al profesional responsable de las citas y atenciones.';

-- Tabla: PROPIETARIO

CREATE TABLE `la_casona_ev01_pruebas`.`PROPIETARIO` (
  `id_propietario` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Identificador del propietario',
  `tipo_documento` VARCHAR(10) NOT NULL COMMENT 'Tipo de identificación',
  `numero_documento` VARCHAR(25) NOT NULL COMMENT 'Número de identificación sin perder ceros',
  `nombres` VARCHAR(80) NOT NULL COMMENT 'Nombres del propietario',
  `apellidos` VARCHAR(80) NOT NULL COMMENT 'Apellidos del propietario',
  `telefono` VARCHAR(25) NOT NULL COMMENT 'Teléfono de contacto',
  `correo` VARCHAR(120) NULL DEFAULT NULL COMMENT 'Correo de contacto',
  `direccion` VARCHAR(150) NULL DEFAULT NULL COMMENT 'Dirección de contacto',
  PRIMARY KEY (`id_propietario`),
  UNIQUE INDEX `uq_propietario_1` (`tipo_documento` ASC, `numero_documento` ASC) VISIBLE)
ENGINE = InnoDB
COMMENT = 'Guarda los datos de la persona responsable de la mascota.';

-- Tabla: MASCOTA

CREATE TABLE `la_casona_ev01_pruebas`.`MASCOTA` (
  `id_mascota` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Identificador del paciente',
  `id_propietario` INT UNSIGNED NOT NULL COMMENT 'Propietario responsable',
  `nombre` VARCHAR(60) NOT NULL COMMENT 'Nombre de la mascota',
  `especie` VARCHAR(40) NOT NULL COMMENT 'Especie del paciente',
  `sexo` VARCHAR(15) NOT NULL COMMENT 'MACHO HEMBRA o NO_DETERMINADO',
  `fecha_nacimiento` DATE NULL DEFAULT NULL COMMENT 'Fecha conocida o estimada de nacimiento',
  PRIMARY KEY (`id_mascota`),
  INDEX `fk_mascota_id_propietario` (`id_propietario` ASC) VISIBLE,
  CONSTRAINT `fk_mascota_id_propietario`
    FOREIGN KEY (`id_propietario`)
    REFERENCES `la_casona_ev01_pruebas`.`PROPIETARIO` (`id_propietario`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
COMMENT = 'Identifica al paciente y lo asocia con su propietario.';

-- Tabla: HISTORIA_CLINICA

CREATE TABLE `la_casona_ev01_pruebas`.`HISTORIA_CLINICA` (
  `id_mascota` INT UNSIGNED NOT NULL COMMENT 'Mascota a la que pertenece la historia',
  `fecha_apertura` DATE NOT NULL COMMENT 'Fecha de apertura',
  PRIMARY KEY (`id_mascota`),
  CONSTRAINT `fk_historia_clinica_id_mascota`
    FOREIGN KEY (`id_mascota`)
    REFERENCES `la_casona_ev01_pruebas`.`MASCOTA` (`id_mascota`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
COMMENT = 'Abre una única historia para cada mascota atendida.';

-- Tabla: CITA

CREATE TABLE `la_casona_ev01_pruebas`.`CITA` (
  `id_cita` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Identificador de la cita',
  `id_mascota` INT UNSIGNED NOT NULL COMMENT 'Mascota programada',
  `id_veterinario` INT UNSIGNED NOT NULL COMMENT 'Profesional asignado',
  `inicio` DATETIME NOT NULL COMMENT 'Fecha y hora de inicio',
  `fin` DATETIME NOT NULL COMMENT 'Fecha y hora de finalización',
  `estado` VARCHAR(15) NOT NULL DEFAULT 'PROGRAMADA' COMMENT 'PROGRAMADA ATENDIDA o CANCELADA',
  `motivo` VARCHAR(250) NOT NULL COMMENT 'Motivo de la cita',
  PRIMARY KEY (`id_cita`),
  INDEX `ix_cita_agenda` (`id_veterinario` ASC, `inicio` ASC, `fin` ASC) VISIBLE,
  INDEX `fk_cita_id_mascota` (`id_mascota` ASC) VISIBLE,
  CONSTRAINT `fk_cita_id_mascota`
    FOREIGN KEY (`id_mascota`)
    REFERENCES `la_casona_ev01_pruebas`.`MASCOTA` (`id_mascota`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `fk_cita_id_veterinario`
    FOREIGN KEY (`id_veterinario`)
    REFERENCES `la_casona_ev01_pruebas`.`VETERINARIO` (`id_veterinario`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
COMMENT = 'Organiza el intervalo reservado con un veterinario.';

-- Tabla: ATENCION

CREATE TABLE `la_casona_ev01_pruebas`.`ATENCION` (
  `id_atencion` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Identificador de la atención',
  `id_mascota` INT UNSIGNED NOT NULL COMMENT 'Historia clínica de la mascota',
  `id_veterinario` INT UNSIGNED NOT NULL COMMENT 'Profesional que realizó la atención',
  `fecha_hora` DATETIME NOT NULL COMMENT 'Fecha y hora de atención',
  `motivo_consulta` VARCHAR(250) NOT NULL COMMENT 'Razón de la consulta',
  `valoracion` TEXT NOT NULL COMMENT 'Descripción de la valoración',
  `recomendaciones` TEXT NULL DEFAULT NULL COMMENT 'Recomendaciones al propietario',
  PRIMARY KEY (`id_atencion`),
  INDEX `fk_atencion_id_mascota` (`id_mascota` ASC) VISIBLE,
  INDEX `fk_atencion_id_veterinario` (`id_veterinario` ASC) VISIBLE,
  CONSTRAINT `fk_atencion_id_mascota`
    FOREIGN KEY (`id_mascota`)
    REFERENCES `la_casona_ev01_pruebas`.`HISTORIA_CLINICA` (`id_mascota`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `fk_atencion_id_veterinario`
    FOREIGN KEY (`id_veterinario`)
    REFERENCES `la_casona_ev01_pruebas`.`VETERINARIO` (`id_veterinario`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
COMMENT = 'Registra lo ocurrido durante un episodio de atención.';

-- Tabla: CITA_ATENCION

CREATE TABLE `la_casona_ev01_pruebas`.`CITA_ATENCION` (
  `id_cita` INT UNSIGNED NOT NULL COMMENT 'Cita que originó la atención',
  `id_atencion` INT UNSIGNED NOT NULL COMMENT 'Atención asociada',
  PRIMARY KEY (`id_cita`),
  UNIQUE INDEX `uq_cita_atencion_1` (`id_atencion` ASC) VISIBLE,
  CONSTRAINT `fk_cita_atencion_id_cita`
    FOREIGN KEY (`id_cita`)
    REFERENCES `la_casona_ev01_pruebas`.`CITA` (`id_cita`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `fk_cita_atencion_id_atencion`
    FOREIGN KEY (`id_atencion`)
    REFERENCES `la_casona_ev01_pruebas`.`ATENCION` (`id_atencion`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
COMMENT = 'Relaciona una cita con su atención cuando existe ese vínculo.';

-- Tabla: DIAGNOSTICO

CREATE TABLE `la_casona_ev01_pruebas`.`DIAGNOSTICO` (
  `id_atencion` INT UNSIGNED NOT NULL COMMENT 'Atención de origen',
  `numero_diagnostico` SMALLINT UNSIGNED NOT NULL COMMENT 'Orden del diagnóstico en esa atención',
  `descripcion` TEXT NOT NULL COMMENT 'Descripción del diagnóstico',
  PRIMARY KEY (`id_atencion`, `numero_diagnostico`),
  CONSTRAINT `fk_diagnostico_id_atencion`
    FOREIGN KEY (`id_atencion`)
    REFERENCES `la_casona_ev01_pruebas`.`ATENCION` (`id_atencion`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
COMMENT = 'Guarda cada diagnóstico en una fila de la atención.';

-- Tabla: TRATAMIENTO

CREATE TABLE `la_casona_ev01_pruebas`.`TRATAMIENTO` (
  `id_tratamiento` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Identificador del tratamiento',
  `id_atencion` INT UNSIGNED NOT NULL COMMENT 'Atención de origen',
  `descripcion` TEXT NOT NULL COMMENT 'Descripción del tratamiento',
  `fecha_inicio` DATE NOT NULL COMMENT 'Fecha de inicio',
  `fecha_fin` DATE NULL DEFAULT NULL COMMENT 'Fecha de finalización cuando se conoce',
  PRIMARY KEY (`id_tratamiento`),
  INDEX `fk_tratamiento_id_atencion` (`id_atencion` ASC) VISIBLE,
  CONSTRAINT `fk_tratamiento_id_atencion`
    FOREIGN KEY (`id_atencion`)
    REFERENCES `la_casona_ev01_pruebas`.`ATENCION` (`id_atencion`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
COMMENT = 'Conserva el tratamiento indicado durante una atención.';

-- Tabla: PRODUCTO

CREATE TABLE `la_casona_ev01_pruebas`.`PRODUCTO` (
  `id_producto` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Identificador del producto',
  `codigo_producto` VARCHAR(30) NOT NULL COMMENT 'Código único del catálogo',
  `nombre` VARCHAR(100) NOT NULL COMMENT 'Nombre del producto',
  `tipo_producto` VARCHAR(15) NOT NULL COMMENT 'MEDICAMENTO u OTRO',
  `unidad_medida` VARCHAR(20) NOT NULL COMMENT 'Unidad usada para controlar cantidades',
  `precio_actual` DECIMAL(12,2) NOT NULL COMMENT 'Precio vigente en pesos',
  `activo` TINYINT NOT NULL DEFAULT 1 COMMENT '1 activo y 0 inactivo',
  PRIMARY KEY (`id_producto`),
  UNIQUE INDEX `uq_producto_1` (`codigo_producto` ASC) VISIBLE)
ENGINE = InnoDB
COMMENT = 'Es el catálogo común de medicamentos y otros productos.';

-- Tabla: PRESCRIPCION

CREATE TABLE `la_casona_ev01_pruebas`.`PRESCRIPCION` (
  `id_tratamiento` INT UNSIGNED NOT NULL COMMENT 'Tratamiento al que pertenece',
  `numero_linea` SMALLINT UNSIGNED NOT NULL COMMENT 'Orden de la línea en el tratamiento',
  `id_producto` INT UNSIGNED NOT NULL COMMENT 'Medicamento indicado',
  `dosis` VARCHAR(100) NOT NULL COMMENT 'Dosis escrita por el profesional',
  `frecuencia` VARCHAR(100) NOT NULL COMMENT 'Frecuencia de administración',
  `duracion` VARCHAR(100) NOT NULL COMMENT 'Duración indicada',
  `via_administracion` VARCHAR(60) NOT NULL COMMENT 'Vía de administración',
  PRIMARY KEY (`id_tratamiento`, `numero_linea`),
  INDEX `fk_prescripcion_id_producto` (`id_producto` ASC) VISIBLE,
  CONSTRAINT `fk_prescripcion_id_tratamiento`
    FOREIGN KEY (`id_tratamiento`)
    REFERENCES `la_casona_ev01_pruebas`.`TRATAMIENTO` (`id_tratamiento`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `fk_prescripcion_id_producto`
    FOREIGN KEY (`id_producto`)
    REFERENCES `la_casona_ev01_pruebas`.`PRODUCTO` (`id_producto`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
COMMENT = 'Guarda un medicamento y sus indicaciones por cada línea.';

-- Tabla: MOVIMIENTO_INVENTARIO

CREATE TABLE `la_casona_ev01_pruebas`.`MOVIMIENTO_INVENTARIO` (
  `id_movimiento` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Identificador del movimiento',
  `id_producto` INT UNSIGNED NOT NULL COMMENT 'Producto que cambia de existencia',
  `id_usuario` INT UNSIGNED NOT NULL COMMENT 'Usuario responsable',
  `fecha_hora` DATETIME NOT NULL COMMENT 'Fecha y hora del movimiento',
  `tipo_movimiento` VARCHAR(7) NOT NULL COMMENT 'ENTRADA o SALIDA',
  `cantidad` DECIMAL(12,3) NOT NULL COMMENT 'Cantidad positiva en la unidad del producto',
  `motivo` VARCHAR(250) NOT NULL COMMENT 'Razón del movimiento',
  PRIMARY KEY (`id_movimiento`),
  INDEX `fk_movimiento_inventario_id_producto` (`id_producto` ASC) VISIBLE,
  INDEX `fk_movimiento_inventario_id_usuario` (`id_usuario` ASC) VISIBLE,
  CONSTRAINT `fk_movimiento_inventario_id_producto`
    FOREIGN KEY (`id_producto`)
    REFERENCES `la_casona_ev01_pruebas`.`PRODUCTO` (`id_producto`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `fk_movimiento_inventario_id_usuario`
    FOREIGN KEY (`id_usuario`)
    REFERENCES `la_casona_ev01_pruebas`.`USUARIO` (`id_usuario`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
COMMENT = 'Registra cada entrada o salida y su responsable.';

-- Tabla: ENTREGA_MEDICAMENTO

CREATE TABLE `la_casona_ev01_pruebas`.`ENTREGA_MEDICAMENTO` (
  `id_movimiento` INT UNSIGNED NOT NULL COMMENT 'Movimiento de salida de la entrega',
  `id_tratamiento` INT UNSIGNED NOT NULL COMMENT 'Tratamiento atendido',
  PRIMARY KEY (`id_movimiento`),
  INDEX `fk_entrega_medicamento_id_tratamiento` (`id_tratamiento` ASC) VISIBLE,
  CONSTRAINT `fk_entrega_medicamento_id_movimiento`
    FOREIGN KEY (`id_movimiento`)
    REFERENCES `la_casona_ev01_pruebas`.`MOVIMIENTO_INVENTARIO` (`id_movimiento`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `fk_entrega_medicamento_id_tratamiento`
    FOREIGN KEY (`id_tratamiento`)
    REFERENCES `la_casona_ev01_pruebas`.`TRATAMIENTO` (`id_tratamiento`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
COMMENT = 'Asocia una salida del inventario con un tratamiento.';

-- Tabla: SERVICIO

CREATE TABLE `la_casona_ev01_pruebas`.`SERVICIO` (
  `id_servicio` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Identificador del servicio',
  `codigo_servicio` VARCHAR(30) NOT NULL COMMENT 'Código único del catálogo',
  `nombre` VARCHAR(100) NOT NULL COMMENT 'Nombre del servicio',
  `tarifa_actual` DECIMAL(12,2) NOT NULL COMMENT 'Tarifa vigente en pesos',
  `activo` TINYINT NOT NULL DEFAULT 1 COMMENT '1 activo y 0 inactivo',
  PRIMARY KEY (`id_servicio`),
  UNIQUE INDEX `uq_servicio_1` (`codigo_servicio` ASC) VISIBLE)
ENGINE = InnoDB
COMMENT = 'Reúne los servicios que la clínica puede facturar.';

-- Tabla: FACTURA

CREATE TABLE `la_casona_ev01_pruebas`.`FACTURA` (
  `id_factura` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Identificador de la factura',
  `numero_factura` VARCHAR(30) NOT NULL COMMENT 'Número único de la factura',
  `id_propietario` INT UNSIGNED NOT NULL COMMENT 'Responsable de la factura',
  `fecha_hora` DATETIME NOT NULL COMMENT 'Fecha y hora de creación',
  `estado` VARCHAR(10) NOT NULL DEFAULT 'BORRADOR' COMMENT 'BORRADOR EMITIDA o ANULADA',
  PRIMARY KEY (`id_factura`),
  UNIQUE INDEX `uq_factura_1` (`numero_factura` ASC) VISIBLE,
  INDEX `fk_factura_id_propietario` (`id_propietario` ASC) VISIBLE,
  CONSTRAINT `fk_factura_id_propietario`
    FOREIGN KEY (`id_propietario`)
    REFERENCES `la_casona_ev01_pruebas`.`PROPIETARIO` (`id_propietario`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
COMMENT = 'Registra la cabecera de la factura interna.';

-- Tabla: DETALLE_FACTURA

CREATE TABLE `la_casona_ev01_pruebas`.`DETALLE_FACTURA` (
  `id_factura` INT UNSIGNED NOT NULL COMMENT 'Factura a la que pertenece',
  `numero_linea` SMALLINT UNSIGNED NOT NULL COMMENT 'Orden de la línea en la factura',
  `id_producto` INT UNSIGNED NULL DEFAULT NULL COMMENT 'Producto cobrado si corresponde',
  `id_servicio` INT UNSIGNED NULL DEFAULT NULL COMMENT 'Servicio cobrado si corresponde',
  `cantidad` DECIMAL(12,3) NOT NULL COMMENT 'Cantidad facturada',
  `precio_unitario_aplicado` DECIMAL(12,2) NOT NULL COMMENT 'Precio histórico de una unidad',
  PRIMARY KEY (`id_factura`, `numero_linea`),
  INDEX `fk_detalle_factura_id_producto` (`id_producto` ASC) VISIBLE,
  INDEX `fk_detalle_factura_id_servicio` (`id_servicio` ASC) VISIBLE,
  CONSTRAINT `fk_detalle_factura_id_factura`
    FOREIGN KEY (`id_factura`)
    REFERENCES `la_casona_ev01_pruebas`.`FACTURA` (`id_factura`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `fk_detalle_factura_id_producto`
    FOREIGN KEY (`id_producto`)
    REFERENCES `la_casona_ev01_pruebas`.`PRODUCTO` (`id_producto`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT,
  CONSTRAINT `fk_detalle_factura_id_servicio`
    FOREIGN KEY (`id_servicio`)
    REFERENCES `la_casona_ev01_pruebas`.`SERVICIO` (`id_servicio`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
COMMENT = 'Guarda los conceptos y los precios aplicados en cada factura.';

-- Tabla: PAGO

CREATE TABLE `la_casona_ev01_pruebas`.`PAGO` (
  `id_pago` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Identificador del pago',
  `id_factura` INT UNSIGNED NOT NULL COMMENT 'Factura que recibe el pago',
  `fecha_hora` DATETIME NOT NULL COMMENT 'Fecha y hora del pago',
  `valor` DECIMAL(12,2) NOT NULL COMMENT 'Valor positivo del abono en pesos',
  `medio_pago` VARCHAR(20) NOT NULL COMMENT 'EFECTIVO TARJETA o TRANSFERENCIA',
  PRIMARY KEY (`id_pago`),
  INDEX `fk_pago_id_factura` (`id_factura` ASC) VISIBLE,
  CONSTRAINT `fk_pago_id_factura`
    FOREIGN KEY (`id_factura`)
    REFERENCES `la_casona_ev01_pruebas`.`FACTURA` (`id_factura`)
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
COMMENT = 'Registra cada abono que se aplica a una factura.';

-- 3. RESTRICCIONES SOBRE LOS VALORES
-- Por ejemplo: cantidades positivas y fecha final posterior al inicio.

ALTER TABLE ROL ADD CONSTRAINT ck_rol_nombre CHECK (nombre_rol IN ('RECEPCION','VETERINARIA','INVENTARIO','ADMINISTRACION'));
ALTER TABLE USUARIO ADD CONSTRAINT ck_usuario_activo CHECK (activo IN (0,1));
ALTER TABLE MASCOTA ADD CONSTRAINT ck_mascota_sexo CHECK (sexo IN ('MACHO','HEMBRA','NO_DETERMINADO'));
ALTER TABLE CITA ADD CONSTRAINT ck_cita_intervalo CHECK (fin > inicio);
ALTER TABLE CITA ADD CONSTRAINT ck_cita_estado CHECK (estado IN ('PROGRAMADA','ATENDIDA','CANCELADA'));
ALTER TABLE DIAGNOSTICO ADD CONSTRAINT ck_diagnostico_numero CHECK (numero_diagnostico > 0);
ALTER TABLE TRATAMIENTO ADD CONSTRAINT ck_tratamiento_fechas CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio);
ALTER TABLE PRODUCTO ADD CONSTRAINT ck_producto_tipo CHECK (tipo_producto IN ('MEDICAMENTO','OTRO'));
ALTER TABLE PRODUCTO ADD CONSTRAINT ck_producto_precio CHECK (precio_actual >= 0);
ALTER TABLE PRODUCTO ADD CONSTRAINT ck_producto_activo CHECK (activo IN (0,1));
ALTER TABLE PRESCRIPCION ADD CONSTRAINT ck_prescripcion_linea CHECK (numero_linea > 0);
ALTER TABLE MOVIMIENTO_INVENTARIO ADD CONSTRAINT ck_movimiento_tipo CHECK (tipo_movimiento IN ('ENTRADA','SALIDA'));
ALTER TABLE MOVIMIENTO_INVENTARIO ADD CONSTRAINT ck_movimiento_cantidad CHECK (cantidad > 0);
ALTER TABLE SERVICIO ADD CONSTRAINT ck_servicio_tarifa CHECK (tarifa_actual >= 0);
ALTER TABLE SERVICIO ADD CONSTRAINT ck_servicio_activo CHECK (activo IN (0,1));
ALTER TABLE FACTURA ADD CONSTRAINT ck_factura_estado CHECK (estado IN ('BORRADOR','EMITIDA','ANULADA'));
ALTER TABLE DETALLE_FACTURA ADD CONSTRAINT ck_detalle_linea CHECK (numero_linea > 0);
ALTER TABLE DETALLE_FACTURA ADD CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0);
ALTER TABLE DETALLE_FACTURA ADD CONSTRAINT ck_detalle_precio CHECK (precio_unitario_aplicado >= 0);
ALTER TABLE DETALLE_FACTURA ADD CONSTRAINT ck_detalle_concepto CHECK ((id_producto IS NOT NULL AND id_servicio IS NULL) OR (id_producto IS NULL AND id_servicio IS NOT NULL));
ALTER TABLE PAGO ADD CONSTRAINT ck_pago_valor CHECK (valor > 0);
ALTER TABLE PAGO ADD CONSTRAINT ck_pago_medio CHECK (medio_pago IN ('EFECTIVO','TARJETA','TRANSFERENCIA'));

-- 4. VISTAS PARA CONSULTAR EXISTENCIAS Y SALDOS
-- Calculan resultados a partir de los registros, sin duplicar esos datos.

USE `la_casona_ev01_pruebas`;
-- La existencia se consulta, no se duplica en PRODUCTO.
CREATE  OR REPLACE VIEW V_EXISTENCIAS AS
SELECT p.id_producto, p.codigo_producto, p.nombre, p.unidad_medida,
       COALESCE(SUM(CASE WHEN m.tipo_movimiento = 'ENTRADA' THEN m.cantidad
                         WHEN m.tipo_movimiento = 'SALIDA' THEN -m.cantidad ELSE 0 END), 0) AS existencia
FROM PRODUCTO p LEFT JOIN MOVIMIENTO_INVENTARIO m ON m.id_producto = p.id_producto
GROUP BY p.id_producto, p.codigo_producto, p.nombre, p.unidad_medida;

USE `la_casona_ev01_pruebas`;
CREATE  OR REPLACE VIEW V_SALDOS_FACTURA AS
SELECT f.id_factura, f.numero_factura, f.estado,
       COALESCE(d.total,0) AS total, COALESCE(p.pagado,0) AS pagado,
       COALESCE(d.total,0)-COALESCE(p.pagado,0) AS saldo
FROM FACTURA f
LEFT JOIN (SELECT id_factura, SUM(ROUND(cantidad*precio_unitario_aplicado,2)) AS total
           FROM DETALLE_FACTURA GROUP BY id_factura) d ON d.id_factura=f.id_factura
LEFT JOIN (SELECT id_factura,SUM(valor) AS pagado FROM PAGO GROUP BY id_factura) p ON p.id_factura=f.id_factura;

-- Fin del script. La estructura queda vacía, lista para registrar información.
