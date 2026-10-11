USE la_casona_ev01;
INSERT INTO PROPIETARIO
    (tipo_documento, numero_documento, nombres, apellidos, telefono, correo, direccion)
VALUES
    ('CC', '1032456789', 'Laura Marcela', 'Gómez Pérez', '3004567812',
     'laura.gomez@example.com', 'Carrera 70 # 45-18, Medellín'),
    ('CC', '1028765432', 'Carlos Andrés', 'Ramírez López', '3115678923',
     'carlos.ramirez@example.com', 'Calle 10 # 43-25, Medellín'),
    ('CC', '1045123987', 'Diana Carolina', 'Restrepo Martínez', '3126789034',
     NULL, 'Carrera 48 # 20-36, Envigado');
INSERT INTO MASCOTA (id_propietario, nombre, especie, sexo, fecha_nacimiento)
SELECT id_propietario, 'Luna', 'Canino', 'HEMBRA', '2022-03-15'
FROM PROPIETARIO WHERE tipo_documento = 'CC' AND numero_documento = '1032456789';
