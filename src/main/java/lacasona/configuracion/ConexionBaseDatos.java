package lacasona.configuracion;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Lee la conexión desde un archivo externo al código. */
public final class ConexionBaseDatos {
    private final String url;
    private final String usuario;
    private final String clave;

    public ConexionBaseDatos(Path archivo) throws IOException {
        Properties propiedades = new Properties();
        try (Reader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            propiedades.load(lector);
        }
        url = propiedades.getProperty("db.url", "");
        usuario = propiedades.getProperty("db.usuario", "");
        clave = propiedades.getProperty("db.clave", "");
        if (!url.startsWith("jdbc:mysql://") || usuario.isBlank()) {
            throw new IOException("Revisa la dirección y el usuario de MySQL en la configuración.");
        }
    }

    public Connection abrir() throws SQLException {
        DriverManager.setLoginTimeout(5);
        return DriverManager.getConnection(url, usuario, clave);
    }
}
