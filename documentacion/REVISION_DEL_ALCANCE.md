# Revisión del alcance antes de programar

**Evidencia GA7-220501096-AA2-EV01 · James Alberto Torres Peláez · Ficha 3186594**

Para preparar esta evidencia revisé la guía 7, los estándares de codificación, la formulación actualizada, los casos de uso, el diseño de la base de datos, la propuesta técnica y la maqueta del proyecto. Separé los ejemplos del material de apoyo de los requisitos propios de La Casona.

## Resultado de la revisión

El CRUD de propietarios en Java para escritorio, conectado a MySQL por JDBC, corresponde al proyecto. La guía 7 pide inserción, consulta, actualización y eliminación, conexión JDBC, uso de versionamiento y entrega del proyecto comprimido con el enlace del repositorio.

El documento de estándares ya propone Java SE, Swing, JDBC y MySQL. La formulación mantiene la aplicación de escritorio con información centralizada. Por eso esta evidencia utiliza una ventana de escritorio y una base MySQL, con la dirección de conexión en un archivo externo.

## Relación entre documentos y módulo

| Documento o requisito | Lo que indica | Aplicación en esta evidencia |
| --- | --- | --- |
| Guía 7, páginas 7 y 8 | CRUD con JDBC y código versionado. | Cuatro operaciones, conector JDBC, repositorio Git y ZIP. |
| RF-04 de la formulación | Registrar, buscar y actualizar propietarios; documento único. | Formulario, búsqueda y validación de duplicados. |
| CU-05 | Sesión de recepción, campos obligatorios y dos opcionales. | Ingreso, permiso en el servicio y formulario con esos campos. |
| RF-25 y reglas de CU-05 | No borrar propietarios con mascotas o facturas. | Llaves foráneas RESTRICT y mensaje de rechazo. |
| Matriz de permisos | Recepción gestiona; veterinaria y administración consultan. | Comprobación del rol antes de cada operación del servicio. |
| Diseño AP4 y modelo físico AP6 | PROPIETARIO conserva ocho columnas, incluyendo el ID. | Mismos nombres, tipos, longitudes y documento compuesto único. |
| Estándares GA7-AA1-EV02 | Paquetes en minúsculas, clases PascalCase, métodos camelCase. | Organización bajo `lacasona` y nombres descriptivos en español. |
| Estándares de acceso a datos | SQL parametrizado, cierre de recursos y credenciales externas. | PreparedStatement, try-with-resources y db.properties excluido. |
| Maqueta AP5 | Consulta y edición de los datos del propietario. | Tabla y formulario con los mismos datos del modelo. |

La eliminación de un propietario sin relaciones se incluye por el alcance CRUD solicitado para esta evidencia. Se pide confirmación y no se eliminan mascotas, historias ni facturas para permitir el borrado.

## Datos que se conservaron

| Campo | Tipo en MySQL | Condición |
| --- | --- | --- |
| id_propietario | INT UNSIGNED | Clave primaria y numeración automática. |
| tipo_documento | VARCHAR(10) | Obligatorio. |
| numero_documento | VARCHAR(25) | Obligatorio; se conserva como texto. |
| nombres | VARCHAR(80) | Obligatorio. |
| apellidos | VARCHAR(80) | Obligatorio. |
| telefono | VARCHAR(25) | Obligatorio. |
| correo | VARCHAR(120) | Opcional. |
| direccion | VARCHAR(150) | Opcional. |

La combinación de tipo y número de documento no se repite. Las mascotas y facturas conservan su referencia al propietario.

## Decisiones tomadas para el módulo

Se utilizó una base nueva llamada `la_casona_ev01`, manteniendo las 19 tablas del modelo físico. El formulario trabaja con PROPIETARIO; las tablas USUARIO y ROL se consultan para el ingreso. No se programaron formularios adicionales de los otros procesos.

El tipo de documento se escribe como texto y debe cumplir la longitud definida. El filtro ofrece CC, CE, TI, PAS, PEP y PPT como opciones de búsqueda, sin impedir que el formulario conserve otro tipo dentro de la longitud del modelo. El teléfono acepta números y separadores comunes, y el correo se revisa cuando se diligencia. Estos formatos todavía pueden ajustarse con la clínica.

Se usó PBKDF2 con sal individual para las cuentas. Es una implementación con las funciones de seguridad incluidas en Java. Las consultas se hacen con SwingWorker para que la ventana siga respondiendo mientras trabaja con MySQL.

Las 32 pruebas realizadas verifican este módulo. No certifican todos los requisitos de rendimiento, copias de seguridad, disponibilidad o trabajo concurrente del sistema completo.

## Documentos consultados

Las rutas siguientes corresponden a los originales del proyecto, que se conservaron:

1. `Fase 3 Ejecucion/Actividad del Proyecto 7/Guia de Aprendizaje 7.pdf`.
2. `Fase 3 Ejecucion/Actividad del Proyecto 7/AP GA7-220501096-AA1-EV02 Definir estándares/GA7-220501096-AA1-EV02 Estandares de codificacion.docx`.
3. `Nivelacion/Plan de mejoramiento James Torres/1. ACTIVIDAD DE PROYECTO 1 (AP1)  Especificaciones Funcionales y Metodología/Formulacion_del_proyecto_Clinica_Veterinaria_La_Casona.docx`.
4. `Nivelacion/Plan de mejoramiento James Torres/2. ACTIVIDAD DE PROYECTO 2 (AP2)  Modelo Conceptual y Propuesta Técnica/Diagrama de casos de uso UML y Descripciones.docx`.
5. `Nivelacion/Plan de mejoramiento James Torres/2. ACTIVIDAD DE PROYECTO 2 (AP2)  Modelo Conceptual y Propuesta Técnica/Propuesta tecnica con costos de infraestructura.docx`.
6. `Nivelacion/Plan de mejoramiento James Torres/4. ACTIVIDAD DE PROYECTO 4 (AP4)  Estructura Lógica, BD y POO/Diagrama Entidad Relacion.docx`.
7. `Fase 3 Ejecucion/Actividad del proyecto 6/Actividad de aprendizaje GA6-220501096-AA2-EV03/01_modelo_fisico_la_casona.sql`.
8. `Entregables/AP5_Maqueta_La_Casona_V1_0_2/documentacion/criterios_y_fuentes.md` y `trazabilidad.md`.

No se encontró una rúbrica adicional en la carpeta de esta evidencia. Se utilizaron los lineamientos de la guía y los estándares propios del proyecto.
