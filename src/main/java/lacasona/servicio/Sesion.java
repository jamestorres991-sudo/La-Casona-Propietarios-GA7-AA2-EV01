package lacasona.servicio;

/** Representa el usuario que ingresó al módulo. */
public final class Sesion {
    private final long idUsuario;
    private final String nombreUsuario;
    private boolean estaAbierta = true;

    Sesion(long idUsuario, String nombreUsuario) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
    }

    public long getIdUsuario() {

        return idUsuario;

    }
    public String getNombreUsuario() {
        return nombreUsuario;
    }
    public boolean estaAbierta() {
        return estaAbierta;
    }

    public void cerrar() {
        estaAbierta = false;
    }
}
