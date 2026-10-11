package lacasona.servicio;

import java.sql.SQLException;
import java.util.List;
import lacasona.modelo.Propietario;
import lacasona.repositorio.PropietarioRepositorio;

/** Revisa los datos y permisos antes de consultar o cambiar un propietario. */
public final class PropietarioServicio {
    private final PropietarioRepositorio repositorio;
    private final AccesoServicio acceso;
    private final Sesion sesion;

    public PropietarioServicio(PropietarioRepositorio repositorio, AccesoServicio acceso,
            Sesion sesion) {
        this.repositorio = repositorio;
        this.acceso = acceso;
        this.sesion = sesion;
    }

    public List<Propietario> consultar(String texto, String tipo, int pagina) throws SQLException {
        acceso.comprobarPermiso(sesion, false);
        if (texto == null || tipo == null || texto.length() > 120 || tipo.length() > 10
                || pagina < 0 || pagina > 1000000) {
            throw new IllegalArgumentException("Revisa los filtros de búsqueda.");
        }
        return repositorio.consultar(texto.strip(), tipo.strip(), pagina);
    }

    public Propietario consultarPorId(long idPropietario) throws SQLException {
        acceso.comprobarPermiso(sesion, false);
        return repositorio.consultarPorId(idPropietario);
    }

    public long guardar(Propietario propietario) throws SQLException {
        acceso.comprobarPermiso(sesion, true);
        validar(propietario);
        if (propietario.getIdPropietario() == 0) {
            return repositorio.insertar(propietario);
        }
        repositorio.actualizar(propietario);
        return propietario.getIdPropietario();
    }

    public void eliminar(long idPropietario) throws SQLException {
        acceso.comprobarPermiso(sesion, true);
        if (idPropietario <= 0) {
            throw new IllegalArgumentException("Selecciona el propietario que deseas eliminar.");
        }
        repositorio.eliminar(idPropietario);
    }

    public static void validar(Propietario propietario) {
        if (propietario.getIdPropietario() < 0) {
            throw new IllegalArgumentException("El identificador no es válido.");
        }
        revisar(propietario.getTipoDocumento(), "Tipo de documento", 10, true);
        revisar(propietario.getNumeroDocumento(), "Número de documento", 25, true);
        revisar(propietario.getNombres(), "Nombres", 80, true);
        revisar(propietario.getApellidos(), "Apellidos", 80, true);
        revisar(propietario.getTelefono(), "Teléfono", 25, true);
        revisar(propietario.getCorreo(), "Correo", 120, false);
        revisar(propietario.getDireccion(), "Dirección", 150, false);
        if (!propietario.getTelefono().matches("[+0-9() .-]{7,25}")
                || propietario.getTelefono().replaceAll("[^0-9]", "").length() < 7) {
            throw new IllegalArgumentException("Revisa el número de teléfono.");
        }
        if (!propietario.getCorreo().isEmpty()
                && !propietario.getCorreo().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new IllegalArgumentException("Revisa el correo, por ejemplo: nombre@correo.com.");
        }
    }

    private static void revisar(String texto, String nombre, int maximo, boolean requerido) {
        if (requerido && texto.isBlank()) {
            throw new IllegalArgumentException(nombre + " es obligatorio.");
        }
        if (texto.length() > maximo || texto.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(nombre + ": máximo " + maximo + " caracteres.");
        }
    }
}
