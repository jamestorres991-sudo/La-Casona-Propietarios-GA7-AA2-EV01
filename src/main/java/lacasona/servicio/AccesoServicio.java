package lacasona.servicio;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import lacasona.configuracion.ConexionBaseDatos;

/** Comprueba el ingreso y el permiso antes de usar el módulo. */
public final class AccesoServicio {
    private final ConexionBaseDatos baseDatos;

    public AccesoServicio(ConexionBaseDatos baseDatos) {
        this.baseDatos = baseDatos;
    }

    public Sesion ingresar(String nombre, char[] clave)
            throws SQLException, GeneralSecurityException {
        String sql = "SELECT id_usuario, clave_hash, activo FROM USUARIO "
                + "WHERE nombre_usuario = ?";
        try (Connection conexion = baseDatos.abrir();
                PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, nombre.strip());
            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next() || resultado.getInt("activo") != 1
                        || !ClaveSegura.verificar(clave, resultado.getString("clave_hash"))) {
                    throw new IllegalArgumentException("Revisa el usuario y la contraseña.");
                }
                return new Sesion(resultado.getLong("id_usuario"), nombre.strip());
            }
        } finally {
            Arrays.fill(clave, '\0');
        }
    }

    public void comprobarPermiso(Sesion sesion, boolean requiereGestion) throws SQLException {
        if (sesion == null || !sesion.estaAbierta()) {
            throw new SecurityException("Debes iniciar sesión para usar el módulo.");
        }
        String sql = "SELECT r.nombre_rol FROM USUARIO u "
                + "JOIN ROL r ON r.id_rol = u.id_rol WHERE u.id_usuario = ? AND u.activo = 1";
        try (Connection conexion = baseDatos.abrir();
                PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setLong(1, sesion.getIdUsuario());
            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) {
                    throw new SecurityException("La cuenta ya no está activa.");
                }
                String rol = resultado.getString("nombre_rol");
                boolean puedeConsultar = rol.equals("RECEPCION") || rol.equals("VETERINARIA")
                        || rol.equals("ADMINISTRACION");
                if (!puedeConsultar || (requiereGestion && !rol.equals("RECEPCION"))) {
                    throw new SecurityException("Tu rol no permite realizar esta operación.");
                }
            }
        }
    }
}
