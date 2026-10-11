# Pruebas realizadas al módulo

**Evidencia GA7-220501096-AA2-EV01 · James Alberto Torres Peláez · Ficha 3186594**

Las pruebas se ejecutaron en MySQL 26.7.0, con JDBC Connector/J 9.7.0 y Java 25.0.2. Se utilizó una base separada llamada **la_casona_ev01_pruebas**, para que la revisión no cambiara los registros de la base del formulario.

## Resultados

| Grupo | Cantidad | Aprobadas | Fallidas |
| --- | ---: | ---: | ---: |
| Funcionamiento con JDBC y MySQL | 24 | 24 | 0 |
| Formulario con componentes Swing | 8 | 8 | 0 |
| Total | 32 | 32 | 0 |

Se revisaron registros guardados, cambios de teléfono y correo, búsqueda por tipo y documento, nombres con tildes, documentos con ceros al inicio, eliminación sin relaciones y rechazo cuando existen mascotas o facturas. También se comprobaron campos vacíos, longitudes, correo, consultas parametrizadas, permisos, cierre de sesión, cuenta inactiva y páginas de resultados.

Las pruebas del formulario ejecutan las acciones de los componentes Swing de la ventana y comprueban sus mensajes y los datos guardados. No corresponden a una evaluación de usabilidad con participantes. Las imágenes de `evidencias/vistas` muestran el formulario durante esa ejecución.

## Reportes

- `evidencias/pruebas_integracion.csv`: 24 casos, con comprobación, resultado esperado, observado y estado.
- `evidencias/pruebas_interfaz.csv`: 8 comprobaciones de la ventana y sus botones.
- `evidencias/vistas`: formulario diligenciado, duplicado, eliminación protegida y consulta.

Un caso aprobado de rechazo significa que el programa impidió correctamente una operación inválida. Por ejemplo, la prueba de eliminación con mascota queda aprobada porque MySQL rechazó el borrado y conservó la relación.

## Cómo repetirlas

1. Preparar una base vacía con `sql/05_estructura_pruebas.sql`. Ese archivo crea únicamente `la_casona_ev01_pruebas`; no borra bases existentes.
2. Crear un usuario de MySQL para esa base, con permisos SELECT, INSERT, UPDATE y DELETE sobre sus tablas. No utilizar la conexión de la base principal para las pruebas.
3. Copiar `config/pruebas.properties.example` como `config/pruebas.properties` y completar usuario, contraseña y puerto.
4. Compilar el proyecto y ejecutar:

```powershell
.\scripts\probar.ps1 -RutaJdk "C:\ruta\jdk"
.\scripts\probar_interfaz.ps1 -RutaJdk "C:\ruta\jdk"
```

La primera ejecución prepara las cuentas y los registros que usa cada caso. Las pruebas de interfaz se ejecutan después de las de funcionamiento y necesitan una sesión de escritorio disponible. Para repetir toda la revisión otra vez, preparar de nuevo **solo la base la_casona_ev01_pruebas** vacía. No eliminar ni reiniciar la base principal.

## Alcance de los resultados

Los reportes corresponden al módulo de propietarios y al ingreso incluido. No se ejecutaron pruebas de carga con cinco sesiones, restauración de respaldos ni pruebas de los módulos que todavía no se han programado.
