package lacasona.herramientas;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import lacasona.configuracion.ConexionBaseDatos;
import lacasona.servicio.ClaveSegura;

/** Permite preparar una cuenta desde la consola de administración. */
public final class CrearUsuario {
    private CrearUsuario() {
    }

    public static void main(String[] argumentos) throws Exception {
        if (argumentos.length != 3 || System.console() == null) {
            System.out.println("Uso: CrearUsuario archivo-configuracion usuario rol");
            System.out.println("Ejecuta esta herramienta desde una consola "
                    + "con entrada disponible.");
            return;
        }
        char[] clave = System.console().readPassword("Contraseña para la cuenta: ");
        try {
            if (clave.length < 10) {
                throw new IllegalArgumentException("Usa una contraseña de al menos 10 caracteres.");
            }
            ConexionBaseDatos baseDatos = new ConexionBaseDatos(Path.of(argumentos[0]));
            String sql = "INSERT INTO USUARIO (id_rol, nombre_usuario, clave_hash, activo) "
                    + "SELECT id_rol, ?, ?, 1 FROM ROL WHERE nombre_rol = ?";
            try (Connection conexion = baseDatos.abrir();
                    PreparedStatement consulta = conexion.prepareStatement(sql)) {
                consulta.setString(1, argumentos[1]);
                consulta.setString(2, ClaveSegura.crear(clave));
                consulta.setString(3, argumentos[2]);
                if (consulta.executeUpdate() != 1) {
                    throw new IllegalArgumentException("No se encontró el rol indicado.");
                }
            }
            System.out.println("Cuenta creada.");
        } finally {
            Arrays.fill(clave, '\0');
        }
    }
}
