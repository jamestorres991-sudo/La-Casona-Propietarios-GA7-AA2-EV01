package lacasona;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.Properties;
import javax.swing.JLabel;
import lacasona.configuracion.ConexionBaseDatos;
import lacasona.servicio.AccesoServicio;

/** Comprueba el inicio con una base nueva y separada del formulario. */
public final class PruebasInicioAutomatico {
    private PruebasInicioAutomatico() {
    }

    public static void main(String[] argumentos) throws Exception {
        Path carpeta = Path.of(argumentos[0]).toAbsolutePath();
        Path configuracion = carpeta.resolve("config/db.properties");
        if (Files.exists(configuracion) || Files.exists(carpeta.resolve("datos"))) {
            throw new IllegalArgumentException("Usa una carpeta nueva para esta prueba.");
        }
        Class<?> inicio = Class.forName("lacasona.herramientas.InicioAutomatico");
        var campoCarpeta = inicio.getDeclaredField("carpeta");
        campoCarpeta.setAccessible(true);
        campoCarpeta.set(null, carpeta);
        var campoMensaje = inicio.getDeclaredField("mensaje");
        campoMensaje.setAccessible(true);
        campoMensaje.set(null, new JLabel());
        var preparar = inicio.getDeclaredMethod("preparar", Path.class);
        preparar.setAccessible(true);
        preparar.invoke(null, configuracion);

        ConexionBaseDatos base = new ConexionBaseDatos(configuracion);
        try (Connection conexion = base.abrir()) {
            comprobarCantidad(conexion, "SELECT COUNT(*) FROM PROPIETARIO", 3);
            comprobarCantidad(conexion, "SELECT COUNT(*) FROM USUARIO", 4);
        }
        AccesoServicio acceso = new AccesoServicio(base);
        var sesion = acceso.ingresar("recepcion", "Casona2026!".toCharArray());
        acceso.comprobarPermiso(sesion, true);
        byte[] anterior = Files.readAllBytes(configuracion);
        preparar.invoke(null, configuracion);
        if (!Arrays.equals(anterior, Files.readAllBytes(configuracion))) {
            throw new Exception("La configuración cambió en la reapertura.");
        }

        Properties servidor = new Properties();
        try (var lector = Files.newBufferedReader(carpeta.resolve("datos/servidor.properties"),
                StandardCharsets.UTF_8)) {
            servidor.load(lector);
        }
        String url = "jdbc:mysql://127.0.0.1:" + servidor.getProperty("puerto")
                + "/?sslMode=REQUIRED";
        try (Connection conexion = DriverManager.getConnection(url, "root",
                servidor.getProperty("claveAdmin"))) {
            comprobarCantidad(conexion, "SELECT COUNT(*) FROM information_schema.tables "
                    + "WHERE table_schema='la_casona_ev01' AND table_type='BASE TABLE'", 19);
            try (var consulta = conexion.createStatement();
                    var resultado = consulta.executeQuery("SELECT @@datadir")) {
                resultado.next();
                Path encontrada = Path.of(resultado.getString(1)).toRealPath();
                if (!encontrada.equals(carpeta.resolve("datos/mysql").toRealPath())) {
                    throw new Exception("La base no pertenece a esta prueba.");
                }
            }
            try (var consulta = conexion.createStatement()) {
                consulta.execute("SHUTDOWN");
            }
        }
        Thread.sleep(1500);
        preparar.invoke(null, configuracion);
        try (Connection conexion = base.abrir()) {
            comprobarCantidad(conexion, "SELECT COUNT(*) FROM PROPIETARIO", 3);
        }
        System.out.println("APROBADO: estructura, registros, ingreso, reapertura "
                + "y reinicio conservando los propietarios.");
        System.exit(0);
    }

    private static void comprobarCantidad(Connection conexion, String sql, int cantidad)
            throws Exception {
        try (var consulta = conexion.createStatement();
                var resultado = consulta.executeQuery(sql)) {
            resultado.next();
            if (resultado.getInt(1) != cantidad) {
                throw new Exception("La cantidad encontrada no corresponde a la esperada.");
            }
        }
    }
}
