package lacasona;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import lacasona.configuracion.ConexionBaseDatos;
import lacasona.modelo.Propietario;
import lacasona.repositorio.PropietarioRepositorio;
import lacasona.servicio.AccesoServicio;
import lacasona.servicio.ClaveSegura;
import lacasona.servicio.PropietarioServicio;
import lacasona.servicio.Sesion;

/** Ejecuta las pruebas del módulo contra una base MySQL separada. */
public final class PruebasIntegracion {
    private final ConexionBaseDatos baseDatos;
    private final AccesoServicio acceso;
    private final PropietarioRepositorio repositorio;
    private PropietarioServicio servicio;
    private final List<String> resultados = new ArrayList<>();
    private int fallidos;
    private long idPrincipal;

    private PruebasIntegracion(Path configuracion) throws Exception {
        baseDatos = new ConexionBaseDatos(configuracion);
        acceso = new AccesoServicio(baseDatos);
        repositorio = new PropietarioRepositorio(baseDatos);
    }

    public static void main(String[] argumentos) throws Exception {
        PruebasIntegracion pruebas = new PruebasIntegracion(Path.of(argumentos[0]));
        pruebas.ejecutar();
        Path salida = Path.of("evidencias/pruebas_integracion.csv");
        Files.createDirectories(salida.getParent());
        Files.write(salida, pruebas.resultados, StandardCharsets.UTF_8);
        System.out.println("Pruebas: " + (pruebas.resultados.size() - 3)
                + ". Fallidas: " + pruebas.fallidos);
        if (pruebas.fallidos > 0) { System.exit(1); }
    }

    private void ejecutar() throws Exception {
        try (Connection conexion = baseDatos.abrir(); Statement consulta = conexion.createStatement();
                ResultSet resultado = consulta.executeQuery("SELECT DATABASE(), VERSION()")) {
            resultado.next();
            if (!resultado.getString(1).equals("la_casona_ev01_pruebas")) {
                throw new IllegalArgumentException("Usa únicamente la base la_casona_ev01_pruebas.");
            }
            resultados.add("Fecha Bogotá;" + OffsetDateTime.now(ZoneOffset.ofHours(-5)));
            resultados.add("Base y versión;" + resultado.getString(1) + " / " + resultado.getString(2));
            resultados.add("Caso;Comprobación;Esperado;Observado;Resultado");
        }
        prepararCuentas();
        Sesion sesion = acceso.ingresar("recepcion", "Casona2026!".toCharArray());
        servicio = new PropietarioServicio(repositorio, acceso, sesion);

        probar("CP-01", "Ingresar con recepción", "Sesión activa", () -> {
            comprobar(sesion.estaAbierta(), "La sesión debe estar abierta");
        });
        probar("CP-02", "Rechazar contraseña incorrecta", "Ingreso rechazado", () -> {
            esperar(IllegalArgumentException.class, 0,
                    () -> acceso.ingresar("recepcion", "Incorrecta".toCharArray()));
        });
        probar("CP-03", "Crear propietario sin datos opcionales", "Registro guardado", () -> {
            idPrincipal = servicio.guardar(datos(0, "0012345678", "María Fernanda", ""));
            comprobar(idPrincipal > 0, "Debe existir un identificador");
            comprobar(servicio.consultarPorId(idPrincipal).getCorreo().isEmpty(), "Correo opcional");
        });
        probar("CP-04", "Consultar documento con ceros y nombres con tildes", "Datos conservados", () -> {
            Propietario propietario = servicio.consultarPorId(idPrincipal);
            comprobar(propietario.getNumeroDocumento().equals("0012345678"), "Ceros conservados");
            comprobar(propietario.getNombres().equals("María Fernanda"), "Tildes conservadas");
        });
        probar("CP-05", "Buscar por documento y tipo", "Una coincidencia", () -> {
            comprobar(servicio.consultar("0012345678", "CC", 0).size() == 1, "Una coincidencia");
            comprobar(servicio.consultar("0012345678", "CE", 0).isEmpty(), "Filtro de tipo");
        });
        probar("CP-06", "Buscar nombres", "Propietario encontrado", () -> {
            comprobar(servicio.consultar("María", "", 0).size() == 1, "Buscar nombres");
        });
        probar("CP-07", "Buscar un documento inexistente", "Lista vacía", () -> {
            comprobar(servicio.consultar("999999999999999", "", 0).isEmpty(), "Sin coincidencias");
        });
        probar("CP-08", "Actualizar teléfono y correo", "Cambios guardados en el mismo ID", () -> {
            Propietario actualizado = new Propietario(idPrincipal, "CC", "0012345678",
                    "María Fernanda", "García López", "+57 301 456 7890",
                    "maria.garcia@example.com", "Calle 30 # 50-12, Medellín");
            comprobar(servicio.guardar(actualizado) == idPrincipal, "Mismo ID");
            Propietario guardado = servicio.consultarPorId(idPrincipal);
            comprobar(guardado.getTelefono().equals("+57 301 456 7890"), "Teléfono actualizado");
            comprobar(guardado.getCorreo().equals("maria.garcia@example.com"), "Correo actualizado");
        });
        probar("CP-09", "Crear documento duplicado", "Rechazo MySQL 1062", () -> {
            esperar(SQLException.class, 1062,
                    () -> servicio.guardar(datos(0, "0012345678", "Juan Carlos", "")));
            comprobar(servicio.consultar("0012345678", "CC", 0).size() == 1, "Sin duplicados");
        });
        probar("CP-10", "Guardar con nombre vacío", "Validación rechazada", () -> {
            esperar(IllegalArgumentException.class, 0,
                    () -> servicio.guardar(datos(0, "1000000001", "   ", "")));
        });
        probar("CP-11", "Superar longitud de nombres", "Validación rechazada", () -> {
            esperar(IllegalArgumentException.class, 0,
                    () -> servicio.guardar(datos(0, "1000000002", "A".repeat(81), "")));
        });
        probar("CP-12", "Guardar correo incorrecto", "Validación rechazada", () -> {
            esperar(IllegalArgumentException.class, 0,
                    () -> servicio.guardar(datos(0, "1000000003", "Andrés", "correo-incompleto")));
        });
        probar("CP-13", "Actualizar hacia un documento duplicado", "Rechazo y datos conservados", () -> {
            long idOtro = servicio.guardar(datos(0, "1000000004", "Camilo", ""));
            esperar(SQLException.class, 1062,
                    () -> servicio.guardar(datos(idOtro, "0012345678", "Camilo", "")));
            comprobar(servicio.consultarPorId(idOtro).getNumeroDocumento().equals("1000000004"),
                    "Documento original conservado");
            servicio.eliminar(idOtro);
        });
        probar("CP-14", "Eliminar propietario sin relaciones", "Registro eliminado", () -> {
            long identificador = servicio.guardar(datos(0, "1000000005", "Daniela", ""));
            servicio.eliminar(identificador);
            esperar(IllegalArgumentException.class, 0, () -> servicio.consultarPorId(identificador));
        });
        probar("CP-15", "Eliminar propietario con mascota", "Rechazo MySQL 1451", () -> {
            agregarRelacion("INSERT INTO MASCOTA (id_propietario, nombre, especie, sexo) "
                    + "VALUES (?, 'Toby', 'Canino', 'MACHO')", idPrincipal);
            esperar(SQLException.class, 1451, () -> servicio.eliminar(idPrincipal));
            comprobar(servicio.consultarPorId(idPrincipal) != null, "Propietario conservado");
            comprobar(contar("MASCOTA", idPrincipal) == 1, "Mascota conservada");
        });
        probar("CP-16", "Eliminar propietario con factura", "Rechazo MySQL 1451", () -> {
            long identificador = servicio.guardar(datos(0, "1000000006", "Santiago", ""));
            agregarRelacion("INSERT INTO FACTURA (id_propietario, numero_factura, fecha_hora) "
                    + "VALUES (?, 'LC-EV01-001', NOW())", identificador);
            esperar(SQLException.class, 1451, () -> servicio.eliminar(identificador));
            comprobar(contar("FACTURA", identificador) == 1, "Factura conservada");
        });
        probar("CP-17", "Consultar texto que contiene SQL", "Sin interpretar el texto como SQL", () -> {
            comprobar(servicio.consultar("' OR 1=1 --", "", 0).isEmpty(), "Consulta parametrizada");
            comprobar(servicio.consultarPorId(idPrincipal) != null, "Tabla conservada");
        });
        probar("CP-18", "Actualizar propietario inexistente", "Operación rechazada", () -> {
            esperar(IllegalArgumentException.class, 0,
                    () -> servicio.guardar(datos(4000000000L, "1000000007", "Valentina", "")));
        });
        probar("CP-19", "Usar administración para modificar", "Permiso rechazado", () -> {
            Sesion administracion = acceso.ingresar("administracion", "Casona2026!".toCharArray());
            PropietarioServicio consulta = new PropietarioServicio(repositorio, acceso, administracion);
            comprobar(consulta.consultarPorId(idPrincipal) != null, "Consulta permitida");
            esperar(SecurityException.class, 0,
                    () -> consulta.guardar(datos(0, "1000000008", "Paula", "")));
            esperar(SecurityException.class, 0, () -> consulta.eliminar(idPrincipal));
        });
        probar("CP-20", "Usar inventario para consultar propietarios completos", "Permiso rechazado", () -> {
            Sesion inventario = acceso.ingresar("inventario", "Casona2026!".toCharArray());
            PropietarioServicio consulta = new PropietarioServicio(repositorio, acceso, inventario);
            esperar(SecurityException.class, 0, () -> consulta.consultar("", "", 0));
        });
        probar("CP-21", "Cerrar sesión y volver a usar el servicio", "Operación rechazada", () -> {
            Sesion temporal = acceso.ingresar("recepcion", "Casona2026!".toCharArray());
            PropietarioServicio consulta = new PropietarioServicio(repositorio, acceso, temporal);
            temporal.cerrar();
            esperar(SecurityException.class, 0, () -> consulta.consultar("", "", 0));
        });
        probar("CP-22", "Desactivar cuenta durante la sesión", "Operación rechazada", () -> {
            ejecutarSql("UPDATE USUARIO SET activo = 0 WHERE nombre_usuario = 'recepcion'");
            try {
                esperar(SecurityException.class, 0, () -> servicio.consultar("", "", 0));
                esperar(IllegalArgumentException.class, 0,
                        () -> acceso.ingresar("recepcion", "Casona2026!".toCharArray()));
            } finally {
                ejecutarSql("UPDATE USUARIO SET activo = 1 WHERE nombre_usuario = 'recepcion'");
            }
        });
        probar("CP-23", "Consultar por páginas de 50 registros", "Páginas sin repetir registros", () -> {
            for (int numero = 0; numero < 55; numero++) {
                servicio.guardar(datos(0, "200000" + numero, "Juan", ""));
            }
            List<Propietario> primera = servicio.consultar("200000", "CC", 0);
            List<Propietario> segunda = servicio.consultar("200000", "CC", 1);
            comprobar(primera.size() == 50 && segunda.size() == 5, "50 y 5 registros");
            comprobar(primera.stream().noneMatch(a -> segunda.stream().anyMatch(
                    b -> a.getIdPropietario() == b.getIdPropietario())), "Sin repeticiones");
        });
        probar("CP-24", "Comprobar contraseñas con sal distinta", "Hashes distintos y verificables", () -> {
            String primera = ClaveSegura.crear("Casona2026!".toCharArray());
            String segunda = ClaveSegura.crear("Casona2026!".toCharArray());
            comprobar(!primera.equals(segunda), "Sales distintas");
            comprobar(ClaveSegura.verificar("Casona2026!".toCharArray(), primera), "Clave correcta");
        });
    }

    private static Propietario datos(long identificador, String documento, String nombres,
            String correo) {
        return new Propietario(identificador, "CC", documento, nombres, "García López",
                "3004567890", correo, "");
    }

    private void prepararCuentas() throws Exception {
        for (String rol : List.of("RECEPCION", "VETERINARIA", "INVENTARIO", "ADMINISTRACION")) {
            ejecutarSql("INSERT INTO ROL (nombre_rol) VALUES ('" + rol + "')");
            try (Connection conexion = baseDatos.abrir();
                    PreparedStatement consulta = conexion.prepareStatement(
                            "INSERT INTO USUARIO (id_rol, nombre_usuario, clave_hash) "
                            + "SELECT id_rol, ?, ? FROM ROL WHERE nombre_rol = ?")) {
                consulta.setString(1, rol.toLowerCase(java.util.Locale.ROOT));
                consulta.setString(2, ClaveSegura.crear("Casona2026!".toCharArray()));
                consulta.setString(3, rol);
                consulta.executeUpdate();
            }
        }
    }

    private void agregarRelacion(String sql, long identificador) throws SQLException {
        try (Connection conexion = baseDatos.abrir();
                PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setLong(1, identificador);
            consulta.executeUpdate();
        }
    }

    private long contar(String tabla, long identificador) throws SQLException {
        // El nombre viene de los casos de prueba, no del formulario.
        try (Connection conexion = baseDatos.abrir();
                PreparedStatement consulta = conexion.prepareStatement(
                        "SELECT COUNT(*) FROM " + tabla + " WHERE id_propietario = ?")) {
            consulta.setLong(1, identificador);
            try (ResultSet resultado = consulta.executeQuery()) {
                resultado.next();
                return resultado.getLong(1);
            }
        }
    }

    private void ejecutarSql(String sql) throws SQLException {
        try (Connection conexion = baseDatos.abrir(); Statement consulta = conexion.createStatement()) {
            consulta.executeUpdate(sql);
        }
    }

    private void probar(String codigo, String descripcion, String esperado, Accion accion) {
        try {
            accion.ejecutar();
            resultados.add(codigo + ";" + descripcion + ";" + esperado + ";"
                    + esperado + ";APROBADO");
            System.out.println(codigo + " APROBADO: " + descripcion);
        } catch (Exception | AssertionError error) {
            fallidos++;
            resultados.add(codigo + ";" + descripcion + ";" + esperado + ";"
                    + error.getClass().getSimpleName() + ";FALLIDO");
            error.printStackTrace();
        }
    }

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) { throw new AssertionError(mensaje); }
    }

    private static void esperar(Class<? extends Exception> clase, int codigo, Accion accion)
            throws Exception {
        try {
            accion.ejecutar();
        } catch (Exception error) {
            if (!clase.isInstance(error)) { throw error; }
            if (codigo != 0 && (!(error instanceof SQLException sql)
                    || sql.getErrorCode() != codigo)) { throw error; }
            return;
        }
        throw new AssertionError("Se esperaba " + clase.getSimpleName());
    }

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws Exception;
    }
}
