-- Cuentas para ejecutar y revisar el módulo.
USE la_casona_ev01;
INSERT INTO ROL (nombre_rol) VALUES ('RECEPCION'), ('VETERINARIA'), ('INVENTARIO'), ('ADMINISTRACION');
INSERT INTO USUARIO (id_rol, nombre_usuario, clave_hash, activo)
SELECT id_rol, 'recepcion', 'pbkdf2$600000$0s7qgOoGIjJS1mu5V15+cQ==$ihe9gWPIIFhr43heaIkzSJvuugVJSDGKoTmerXCn97g=', 1
FROM ROL WHERE nombre_rol = 'RECEPCION';
INSERT INTO USUARIO (id_rol, nombre_usuario, clave_hash, activo)
SELECT id_rol, 'veterinaria', 'pbkdf2$600000$CiIGEn/3HUE1B4IVR5nYCg==$VytXHsQgqWJtQkM3vJFNdof4F07QYwyVFedE3xxOLfQ=', 1
FROM ROL WHERE nombre_rol = 'VETERINARIA';
INSERT INTO USUARIO (id_rol, nombre_usuario, clave_hash, activo)
SELECT id_rol, 'inventario', 'pbkdf2$600000$IAiFcITxl/lv35MvtIEKgQ==$Ir6GerpfjHAD4oNFcrGYvJieq8LhnVLEw3MNXudBPbM=', 1
FROM ROL WHERE nombre_rol = 'INVENTARIO';
INSERT INTO USUARIO (id_rol, nombre_usuario, clave_hash, activo)
SELECT id_rol, 'administracion', 'pbkdf2$600000$jzgjWhhAWsJdxyUis8B2lQ==$0PNyRKVxsnKExG4UcHBRLSFQkVNcFmKwRGAUUHKe87Q=', 1
FROM ROL WHERE nombre_rol = 'ADMINISTRACION';
