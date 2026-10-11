-- Completar la contraseña antes de ejecutar este archivo.
-- Ejecutar con la cuenta administradora de MySQL.
CREATE USER 'lc_propietarios'@'127.0.0.1' IDENTIFIED BY 'CAMBIAR_ESTA_CONTRASENA';
CREATE USER 'lc_propietarios'@'localhost' IDENTIFIED BY 'CAMBIAR_ESTA_CONTRASENA';
GRANT SELECT, INSERT, UPDATE, DELETE ON la_casona_ev01.PROPIETARIO
    TO 'lc_propietarios'@'127.0.0.1', 'lc_propietarios'@'localhost';
GRANT SELECT ON la_casona_ev01.USUARIO
    TO 'lc_propietarios'@'127.0.0.1', 'lc_propietarios'@'localhost';
GRANT SELECT ON la_casona_ev01.ROL
    TO 'lc_propietarios'@'127.0.0.1', 'lc_propietarios'@'localhost';
