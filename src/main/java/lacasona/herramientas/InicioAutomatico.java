package lacasona.herramientas;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.Comparator;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import lacasona.Aplicacion;
import lacasona.configuracion.ConexionBaseDatos;

/** Prepara la conexión al abrir el archivo La Casona.exe. */
public final class InicioAutomatico {
    private static Path carpeta;
    private static JFrame aviso;
    private static JLabel mensaje;

    private InicioAutomatico() {
    }

    public static void main(String[] argumentos) {
        try {
            carpeta = Path.of(argumentos[0]).toAbsolutePath().normalize();
            SwingUtilities.invokeAndWait(() -> {
                aviso = new JFrame("La Casona · Inicio");
                mensaje = new JLabel("Preparando la aplicación, espera un momento...",
                        JLabel.CENTER);
                aviso.add(mensaje);
                aviso.setSize(520, 130);
                aviso.setLocationRelativeTo(null);
                aviso.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                aviso.setVisible(true);
            });
            Files.createDirectories(carpeta.resolve("datos"));
            Path configuracion = carpeta.resolve("config/db.properties");
            try (FileChannel canal = FileChannel.open(carpeta.resolve("datos/inicio.lock"),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                    FileLock bloqueo = canal.tryLock()) {
                if (bloqueo == null) {
                    throw new IOException("La aplicación ya se está preparando. "
                            + "Espera un momento.");
                }
                preparar(configuracion);
            }
            SwingUtilities.invokeAndWait(() -> aviso.dispose());
            Aplicacion.main(new String[] {configuracion.toString()});
        } catch (Exception error) {
            if (aviso != null) { aviso.dispose(); }
            String detalle = error instanceof IOException ? error.getMessage()
                    : "Revisa que Java y MySQL estén disponibles. "
                    + "El detalle del inicio está en datos/inicio.log.";
            try {
                if (carpeta != null) {
                    Files.createDirectories(carpeta.resolve("datos"));
                    Files.writeString(carpeta.resolve("datos/inicio.log"),
                            error.getClass().getSimpleName() + ": " + error.getMessage(),
                            StandardCharsets.UTF_8);
                }
            } catch (IOException fallo) {
                System.err.println("No se pudo guardar el detalle del inicio.");
            }
            JOptionPane.showMessageDialog(null, detalle, "Revisar inicio",
                    JOptionPane.WARNING_MESSAGE);
            System.exit(1);
        }
    }

    private static void preparar(Path configuracion) throws Exception {
        if (Files.exists(configuracion)) {
            try (Connection conexion = new ConexionBaseDatos(configuracion).abrir()) {
                return;
            } catch (SQLException error) {
                if (!Files.exists(carpeta.resolve("datos/servidor.properties"))) {
                    throw new IOException("No se pudo conectar con la base configurada. "
                            + "Inicia su servidor MySQL y vuelve a abrir la aplicación.");
                }
            }
        }
        Path archivoServidor = carpeta.resolve("datos/servidor.properties");
        Properties servidor = new Properties();
        if (Files.exists(archivoServidor)) {
            try (Reader lector = Files.newBufferedReader(archivoServidor, StandardCharsets.UTF_8)) {
                servidor.load(lector);
            }
        } else {
            try (ServerSocket disponible = new ServerSocket(0, 1,
                    InetAddress.getByName("127.0.0.1"))) {
                servidor.setProperty("puerto", String.valueOf(disponible.getLocalPort()));
            }
            servidor.setProperty("claveAdmin", crearClave());
            servidor.setProperty("claveConexion", crearClave());
            guardarPropiedades(archivoServidor, servidor);
        }
        int puerto = Integer.parseInt(servidor.getProperty("puerto"));
        Path mysql = buscarMySql();
        Path datos = carpeta.resolve("datos/mysql");
        if (!Files.exists(datos.resolve("auto.cnf"))) {
            mostrar("Preparando una base nueva para La Casona. Esto tarda un momento...");
            Files.createDirectories(datos);
            Process inicio = new ProcessBuilder(mysql.toString(), "--no-defaults",
                    "--initialize-insecure", "--basedir=" + mysql.getParent().getParent(),
                    "--datadir=" + datos).redirectErrorStream(true)
                    .redirectOutput(carpeta.resolve("datos/preparacion_mysql.log").toFile())
                    .start();
            if (!inicio.waitFor(120, TimeUnit.SECONDS) || inicio.exitValue() != 0) {
                throw new IOException("MySQL no pudo preparar la base. "
                        + "Revisa datos/preparacion_mysql.log.");
            }
        }
        if (!estaOcupado(puerto)) {
            mostrar("Iniciando la base de datos de La Casona...");
            new ProcessBuilder(mysql.toString(), "--no-defaults",
                    "--basedir=" + mysql.getParent().getParent(), "--datadir=" + datos,
                    "--bind-address=127.0.0.1", "--port=" + puerto, "--mysqlx=OFF",
                    "--log-error=" + carpeta.resolve("datos/mysql.log"))
                    .redirectErrorStream(true)
                    .redirectOutput(carpeta.resolve("datos/servidor_salida.log").toFile()).start();
        }
        String url = "jdbc:mysql://127.0.0.1:" + puerto
                + "/?sslMode=REQUIRED&connectTimeout=1500&socketTimeout=10000";
        try (Connection conexion = esperarServidor(url, servidor.getProperty("claveAdmin"))) {
            try (Statement revision = conexion.createStatement();
                    ResultSet resultado = revision.executeQuery("SELECT @@datadir")) {
                resultado.next();
                Path encontrada = Path.of(resultado.getString(1)).toRealPath();
                if (!encontrada.equals(datos.toRealPath())) {
                    throw new IOException("El puerto está ocupado por otra base de datos. "
                            + "No se cambió su configuración.");
                }
            }
            mostrar("Revisando las tablas y los datos del módulo...");
            try (Statement consulta = conexion.createStatement()) {
                consulta.executeUpdate("ALTER USER 'root'@'localhost' IDENTIFIED BY '"
                        + servidor.getProperty("claveAdmin") + "'");
                boolean existe;
                try (ResultSet resultado = consulta.executeQuery(
                        "SELECT COUNT(*) FROM information_schema.schemata "
                        + "WHERE schema_name = 'la_casona_ev01'")) {
                    resultado.next();
                    existe = resultado.getInt(1) == 1;
                }
                if (!existe) {
                    ejecutarArchivo(consulta, "01_estructura.sql");
                    ejecutarArchivo(consulta, "02_propietarios.sql");
                    ejecutarArchivo(consulta, "03_cuentas.sql");
                }
                consulta.executeUpdate("CREATE USER IF NOT EXISTS "
                        + "'lc_propietarios'@'127.0.0.1' IDENTIFIED BY '"
                        + servidor.getProperty("claveConexion") + "'");
                consulta.executeUpdate("GRANT SELECT, INSERT, UPDATE, DELETE "
                        + "ON la_casona_ev01.PROPIETARIO TO 'lc_propietarios'@'127.0.0.1'");
                consulta.executeUpdate("GRANT SELECT ON la_casona_ev01.USUARIO "
                        + "TO 'lc_propietarios'@'127.0.0.1'");
                consulta.executeUpdate("GRANT SELECT ON la_casona_ev01.ROL "
                        + "TO 'lc_propietarios'@'127.0.0.1'");
            }
        }
        Properties conexion = new Properties();
        conexion.setProperty("db.url", "jdbc:mysql://127.0.0.1:" + puerto
                + "/la_casona_ev01?sslMode=REQUIRED&connectTimeout=5000&socketTimeout=10000");
        conexion.setProperty("db.usuario", "lc_propietarios");
        conexion.setProperty("db.clave", servidor.getProperty("claveConexion"));
        guardarPropiedades(configuracion, conexion);
        try (Connection comprobacion = new ConexionBaseDatos(configuracion).abrir()) {
            mostrar("La aplicación está lista. Abriendo el ingreso...");
        }
    }

    private static Connection esperarServidor(String url, String clave) throws Exception {
        for (int intento = 0; intento < 60; intento++) {
            try {
                return DriverManager.getConnection(url, "root", clave);
            } catch (SQLException error) {
                try {
                    // Esta cuenta pertenece únicamente a la base nueva creada por el lanzador.
                    return DriverManager.getConnection(url, "root", "");
                } catch (SQLException espera) {
                    Thread.sleep(500);
                }
            }
        }
        throw new IOException("La base no pudo iniciar. Revisa datos/mysql.log. "
                + "No se modificó otro servidor MySQL del equipo.");
    }

    private static void ejecutarArchivo(Statement consulta, String nombre) throws Exception {
        String texto = Files.readString(carpeta.resolve("sql").resolve(nombre),
                StandardCharsets.UTF_8).replaceAll("(?m)^\\s*--.*$", "");
        for (String instruccion : texto.split(";")) {
            if (!instruccion.isBlank()) { consulta.execute(instruccion); }
        }
    }

    private static Path buscarMySql() throws IOException {
        String programas = System.getenv("ProgramFiles");
        Path directorio = Path.of(programas == null ? "C:/Program Files" : programas,
                "MySQL");
        if (Files.isDirectory(directorio)) {
            try (var carpetas = Files.list(directorio)) {
                var encontrado = carpetas.sorted(Comparator.reverseOrder())
                        .map(ruta -> ruta.resolve("bin/mysqld.exe"))
                        .filter(Files::isRegularFile).findFirst();
                if (encontrado.isPresent()) { return encontrado.get(); }
            }
        }
        throw new IOException("No se encontró el servidor MySQL. "
                + "Instala MySQL Server para poder abrir el módulo con este lanzador.");
    }

    private static boolean estaOcupado(int puerto) {
        try (Socket conexion = new Socket()) {
            conexion.connect(new java.net.InetSocketAddress("127.0.0.1", puerto), 500);
            return true;
        } catch (IOException error) {
            return false;
        }
    }

    private static String crearClave() {
        byte[] bytes = new byte[28];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static void guardarPropiedades(Path archivo, Properties propiedades) throws IOException {
        Files.createDirectories(archivo.getParent());
        try (Writer escritor = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8)) {
            propiedades.store(escritor, "Configuración local de La Casona");
        }
    }

    private static void mostrar(String texto) {
        SwingUtilities.invokeLater(() -> mensaje.setText(texto));
    }
}
