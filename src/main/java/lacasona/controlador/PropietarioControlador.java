package lacasona.controlador;

import java.sql.SQLException;
import java.util.List;
import lacasona.modelo.Propietario;
import lacasona.servicio.PropietarioServicio;

/** Lleva las acciones del formulario al servicio. */
public final class PropietarioControlador {
    private final PropietarioServicio servicio;

    public PropietarioControlador(PropietarioServicio servicio) {
        this.servicio = servicio;
    }

    public List<Propietario> buscar(String texto, String tipo, int pagina) throws SQLException {
        return servicio.consultar(texto, tipo, pagina);
    }

    public long guardar(Propietario propietario) throws SQLException {
        return servicio.guardar(propietario);
    }

    public void eliminar(long idPropietario) throws SQLException {
        servicio.eliminar(idPropietario);
    }
}
