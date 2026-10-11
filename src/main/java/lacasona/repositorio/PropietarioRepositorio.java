package lacasona.repositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import lacasona.configuracion.ConexionBaseDatos;
import lacasona.modelo.Propietario;

/** Aquí se encuentran las consultas JDBC del propietario. */
public final class PropietarioRepositorio {
    private static final String CAMPOS = "id_propietario, tipo_documento, numero_documento, "
            + "nombres, apellidos, telefono, correo, direccion";
    private final ConexionBaseDatos baseDatos;

    public PropietarioRepositorio(ConexionBaseDatos baseDatos) {
        this.baseDatos = baseDatos;
    }

    public List<Propietario> consultar(String texto, String tipo, int pagina) throws SQLException {
        String sql = "SELECT " + CAMPOS + " FROM PROPIETARIO WHERE "
                + "(? = '' OR tipo_documento = ?) AND "
                + "(LOCATE(?, numero_documento) > 0 OR LOCATE(?, nombres) > 0 "
                + "OR LOCATE(?, apellidos) > 0) ORDER BY id_propietario LIMIT 50 OFFSET ?";
        List<Propietario> propietarios = new ArrayList<>();
        try (Connection conexion = baseDatos.abrir();
                PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, tipo);
            consulta.setString(2, tipo);
            for (int posicion = 3; posicion <= 5; posicion++) {
                consulta.setString(posicion, texto);
            }
            consulta.setInt(6, Math.multiplyExact(pagina, 50));
            try (ResultSet resultado = consulta.executeQuery()) {
                while (resultado.next()) {
                    propietarios.add(leer(resultado));
                }
            }
        }
        return propietarios;
    }

    public Propietario consultarPorId(long idPropietario) throws SQLException {
        try (Connection conexion = baseDatos.abrir();
                PreparedStatement consulta = conexion.prepareStatement(
                        "SELECT " + CAMPOS + " FROM PROPIETARIO WHERE id_propietario = ?")) {
            consulta.setLong(1, idPropietario);
            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) {
                    throw new IllegalArgumentException("El propietario ya no existe.");
                }
                return leer(resultado);
            }
        }
    }

    public long insertar(Propietario propietario) throws SQLException {
        String sql = "INSERT INTO PROPIETARIO (tipo_documento, numero_documento, nombres, "
                + "apellidos, telefono, correo, direccion) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = baseDatos.abrir();
                PreparedStatement consulta = conexion.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {
            asignar(consulta, propietario);
            consulta.executeUpdate();
            try (ResultSet claves = consulta.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("No se pudo recuperar el identificador.");
                }
                return claves.getLong(1);
            }
        }
    }

    public void actualizar(Propietario propietario) throws SQLException {
        String sql = "UPDATE PROPIETARIO SET tipo_documento = ?, numero_documento = ?, "
                + "nombres = ?, apellidos = ?, telefono = ?, correo = ?, direccion = ? "
                + "WHERE id_propietario = ?";
        try (Connection conexion = baseDatos.abrir();
                PreparedStatement consulta = conexion.prepareStatement(sql)) {
            asignar(consulta, propietario);
            consulta.setLong(8, propietario.getIdPropietario());
            if (consulta.executeUpdate() == 0) {
                throw new IllegalArgumentException("El propietario ya no existe.");
            }
        }
    }

    public void eliminar(long idPropietario) throws SQLException {
        // MySQL impide el borrado si una mascota o factura usa este propietario.
        try (Connection conexion = baseDatos.abrir();
                PreparedStatement consulta = conexion.prepareStatement(
                        "DELETE FROM PROPIETARIO WHERE id_propietario = ?")) {
            consulta.setLong(1, idPropietario);
            if (consulta.executeUpdate() != 1) {
                throw new IllegalArgumentException("El propietario ya no existe.");
            }
        }
    }

    private static void asignar(PreparedStatement consulta, Propietario propietario)
            throws SQLException {
        consulta.setString(1, propietario.getTipoDocumento());
        consulta.setString(2, propietario.getNumeroDocumento());
        consulta.setString(3, propietario.getNombres());
        consulta.setString(4, propietario.getApellidos());
        consulta.setString(5, propietario.getTelefono());
        opcional(consulta, 6, propietario.getCorreo());
        opcional(consulta, 7, propietario.getDireccion());
    }

    private static void opcional(PreparedStatement consulta, int posicion, String texto)
            throws SQLException {
        if (texto.isEmpty()) {
            consulta.setNull(posicion, Types.VARCHAR);
        } else {
            consulta.setString(posicion, texto);
        }
    }

    private static Propietario leer(ResultSet resultado) throws SQLException {
        return new Propietario(resultado.getLong("id_propietario"),
                resultado.getString("tipo_documento"), resultado.getString("numero_documento"),
                resultado.getString("nombres"), resultado.getString("apellidos"),
                resultado.getString("telefono"), resultado.getString("correo"),
                resultado.getString("direccion"));
    }
}
