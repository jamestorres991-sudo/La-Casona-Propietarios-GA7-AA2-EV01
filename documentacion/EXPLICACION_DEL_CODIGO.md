# Cómo está organizado el código

En este módulo separé el formulario de las validaciones y las consultas. Esto permite encontrar con más facilidad dónde se debe corregir cada parte.

| Parte | Archivo | Para qué sirve |
| --- | --- | --- |
| Inicio | Aplicacion.java | Lee la configuración, pide el ingreso y abre la ventana. |
| Modelo | Propietario.java | Guarda los datos de un propietario dentro del programa. |
| Vista | PropietarioVentana.java | Muestra la tabla, los campos, los botones y los mensajes. |
| Controlador | PropietarioControlador.java | Recibe las acciones del formulario y llama al servicio. |
| Servicio | PropietarioServicio.java | Revisa campos y permisos antes de realizar una operación. |
| Repositorio | PropietarioRepositorio.java | Ejecuta INSERT, SELECT, UPDATE y DELETE con JDBC. |
| Conexión | ConexionBaseDatos.java | Abre la conexión con los datos del archivo externo. |
| Ingreso | AccesoServicio.java, Sesion.java y ClaveSegura.java | Comprueban la cuenta, el rol y la contraseña. |

## Ejemplo: registrar un propietario

1. La persona completa el formulario y oprime Guardar.
2. La vista crea un objeto Propietario con los datos ingresados.
3. El controlador envía ese objeto al servicio.
4. El servicio comprueba el rol y revisa los campos obligatorios y sus longitudes.
5. El repositorio prepara el INSERT y asigna cada valor a su parámetro.
6. MySQL guarda la fila y genera su ID.
7. El programa informa el resultado y vuelve a consultar la tabla.

Si el ID es cero, se registra un propietario nuevo. Si el formulario tiene el ID de una fila seleccionada, se actualiza ese registro.

## ¿Por qué se usa PreparedStatement?

Permite enviar por separado la consulta y los valores del formulario. Así un texto escrito en la búsqueda no se interpreta como una instrucción SQL. También facilita usar parámetros del tipo correspondiente.

## ¿Por qué se cierran las conexiones?

Cada operación abre los recursos que necesita y los cierra con try-with-resources. Esto ayuda a no dejar conexiones y consultas abiertas después de terminar.

## ¿Cómo se protege una eliminación?

El formulario pide confirmación. Después el servicio revisa el permiso y el repositorio intenta el DELETE. Si una mascota o factura tiene ese propietario, MySQL rechaza la operación por la llave foránea y la vista muestra el motivo.

## ¿Cómo se manejan los errores?

Los campos incorrectos muestran un mensaje para corregirlos. Un documento duplicado muestra que ya existe. Un problema de conexión no se presenta como una búsqueda sin resultados ni como un guardado exitoso.

## Estándares utilizados

Los paquetes se escriben en minúsculas, las clases comienzan con mayúscula y los métodos usan nombres como guardar, consultar y eliminar. El código usa UTF-8, sangría de cuatro espacios y nombres descriptivos en español. Los comentarios explican la función de las partes sin repetir cada instrucción.
