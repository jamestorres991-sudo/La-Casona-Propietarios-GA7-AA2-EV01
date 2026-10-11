# Cómo preparé el ejecutable

Agregué **La Casona.exe** para abrir el módulo con doble clic. El código principal sigue en Java.
El archivo `lanzador/LaCasona.cs` busca Java y abre `InicioAutomatico`, que revisa la conexión.

Cuando se extrae una copia nueva del ZIP, el inicio prepara una base MySQL en la carpeta `datos`.
Utiliza un puerto local disponible, crea las tablas del modelo y registra las cuentas de ingreso.
También crea contraseñas distintas para la administración de esa base y para la conexión del módulo.
La cuenta del módulo mantiene permisos únicamente sobre las tablas que necesita.

Antes de cambiar una cuenta de administración, el inicio comprueba que la carpeta de la base
corresponda a la que creó el lanzador. La conexión existente se conserva cuando está disponible.

Para volver a construir el ejecutable desde el código, abrir PowerShell en el proyecto y ejecutar:

```powershell
.\scripts\crear_exe.ps1
```

Este archivo compila Java y utiliza el compilador de .NET Framework disponible en Windows.
El ejecutable y las carpetas `dist`, `lib` y `sql` deben mantenerse juntos.

## Comprobación del inicio

Se probó el inicio en una carpeta nueva, la creación de la estructura y los registros,
el ingreso de recepción, la reapertura y el reinicio de su servidor local. Las seis comprobaciones
quedaron aprobadas. El reporte está en `evidencias/pruebas_inicio_automatico.csv`.

Para repetir esta comprobación, ejecutar `scripts/probar_inicio.ps1`. La prueba crea una carpeta
nueva dentro de `target` y reinicia únicamente su propia base; no utiliza la base del formulario.
