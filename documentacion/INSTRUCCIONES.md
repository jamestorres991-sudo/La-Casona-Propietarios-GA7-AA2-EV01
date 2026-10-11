# Instrucciones para ejecutar el módulo

**Evidencia GA7-220501096-AA2-EV01**  
**Aprendiz:** James Alberto Torres Peláez  
**Programa:** Análisis y Desarrollo de Software · **Ficha:** 3186594

## 1. Revisar los programas

Se necesita Java JDK 17 o superior y el servidor MySQL. MySQL Workbench sirve para abrir y ejecutar los scripts, pero el servidor debe estar iniciado para que la aplicación pueda conectarse.

En el equipo donde se realizó la evidencia se utilizó el JDK 25.0.2 de Android Studio y MySQL 26.7.0. La base de esta entrega se preparó aparte, en el puerto 33317. En otro equipo MySQL puede usar el puerto 3306; ese dato se cambia en la configuración.

## 2. Crear la base

En MySQL Workbench, abrir una conexión con permisos para crear bases y ejecutar en este orden:

1. `sql/01_estructura.sql`: crea la base **la_casona_ev01** y sus 19 tablas.
2. `sql/02_propietarios.sql`: registra tres propietarios y la mascota Luna.
3. `sql/03_cuentas.sql`: registra los cuatro roles y las cuentas para ingresar.

Estos archivos se ejecutan una vez en una base nueva. Si la base ya existe, no se debe volver a importar la estructura ni borrar información para repetir estos pasos.

Laura Marcela Gómez Pérez tiene a Luna asociada. Por eso su registro permite comprobar que el módulo impide una eliminación que afectaría a una mascota.

## 3. Preparar el usuario de MySQL

Abrir `sql/04_usuario_conexion.sql`, reemplazar el texto indicado por una contraseña propia y ejecutar el archivo con la cuenta administradora de MySQL. El usuario `lc_propietarios` puede trabajar con PROPIETARIO y consultar las cuentas y roles necesarios para el ingreso.

La cuenta de MySQL conecta el programa con la base. Es diferente del usuario `recepcion`, que se escribe en la ventana de ingreso.

## 4. Configurar la conexión

Copiar `config/db.properties.example` y nombrar la copia **db.properties**, en la misma carpeta. Completar estos datos:

```properties
db.url=jdbc:mysql://127.0.0.1:3306/la_casona_ev01?sslMode=REQUIRED&connectTimeout=5000&socketTimeout=10000
db.usuario=lc_propietarios
db.clave=ESCRIBIR_LA_CONTRASEÑA_DE_MYSQL
```

Cambiar el puerto si el servidor usa otro. `sslMode=REQUIRED` solicita una conexión cifrada en la revisión local. Para conectar equipos de la clínica a un servidor remoto, se debe configurar su certificado y utilizar `sslMode=VERIFY_IDENTITY`, con una dirección que coincida con ese certificado.

`db.properties` se conserva en el equipo y no se incluye en GitHub ni en el ZIP. El proyecto trae el archivo de ejemplo para que cada equipo complete su conexión.

## 5. Compilar

Abrir PowerShell en la carpeta del proyecto:

```powershell
.\scripts\compilar.ps1 -RutaJdk "C:\Program Files\Android\Android Studio\jbr"
```

Si se utiliza otro JDK, cambiar la ruta. Este paso crea `target/propietarios.jar`. El script usa los archivos fuente y no necesita Maven.

Para abrir el proyecto en NetBeans, seleccionar **Abrir proyecto** y escoger la carpeta donde está `pom.xml`. Si Maven está disponible, `mvn compile` también permite compilar. Las pruebas incluidas se ejecutan con los scripts explicados en PRUEBAS.md.

## 6. Ejecutar

```powershell
.\scripts\ejecutar.ps1 -RutaJdk "C:\Program Files\Android\Android Studio\jbr"
```

Ingresar con **recepcion** y **Casona2026!**. Usar esta cuenta para realizar las cuatro operaciones del CRUD. Las cuentas `veterinaria` y `administracion` permiten consultar, pero el servicio rechaza sus intentos de guardar o eliminar. La cuenta `inventario` no ingresa a este módulo.

## 7. Usar el formulario

1. **Nuevo:** limpia el formulario para registrar otro propietario.
2. **Guardar:** crea un registro nuevo o actualiza el que se seleccionó en la tabla.
3. **Buscar:** consulta por documento, nombres o apellidos. Dejar el texto vacío muestra la primera página.
4. **Anterior y Siguiente:** permiten recorrer los resultados de 50 en 50.
5. **Eliminar:** solicita confirmación y elimina solo si el propietario no tiene mascotas ni facturas.
6. **Cerrar sesión:** termina la sesión y cierra la ventana.

El ID se genera en MySQL. Para actualizar se selecciona la fila de la tabla y se cambian los datos del formulario. Correo y dirección se pueden dejar vacíos.

## 8. Si aparece un mensaje de error

Si no conecta, revisar que MySQL esté iniciado, que el puerto sea correcto y que la cuenta de conexión tenga los permisos necesarios. Si aparece un documento duplicado, buscar el propietario existente. Si se rechaza una eliminación, conservar el registro relacionado.

Si se interrumpe una conexión durante un guardado, buscar primero el propietario para conocer su estado antes de volver a guardar. El programa no muestra un guardado exitoso cuando recibe un error.

## 9. Crear otra cuenta de ingreso

Se incluye la herramienta `lacasona.herramientas.CrearUsuario`. Se ejecuta desde una consola con un archivo de conexión administrativo que se conserve fuera del repositorio:

```powershell
& "C:\ruta\jdk\bin\java.exe" -cp "target/propietarios.jar;lib/mysql-connector-j-9.7.0.jar" lacasona.herramientas.CrearUsuario "C:\ruta\conexion-administrativa.properties" nueva_recepcion RECEPCION
```

La herramienta pide la contraseña sin mostrarla y guarda su transformación. No se necesita esta herramienta para ingresar con las cuentas incluidas en `03_cuentas.sql`.

La carpeta `dist` incluye el módulo compilado. Después de configurar la base, `scripts/ejecutar.ps1` permite abrirlo sin volver a compilar. El código fuente también está incluido para revisarlo y realizar cambios.
