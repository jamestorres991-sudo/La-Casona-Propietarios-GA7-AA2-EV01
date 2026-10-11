package lacasona.modelo;

/** Guarda los datos que se muestran en el formulario de propietarios. */
public final class Propietario {
    private final long idPropietario;
    private final String tipoDocumento;
    private final String numeroDocumento;
    private final String nombres;
    private final String apellidos;
    private final String telefono;
    private final String correo;
    private final String direccion;

    public Propietario(long idPropietario, String tipoDocumento, String numeroDocumento,
            String nombres, String apellidos, String telefono, String correo, String direccion) {
        this.idPropietario = idPropietario;
        this.tipoDocumento = limpiar(tipoDocumento);
        this.numeroDocumento = limpiar(numeroDocumento);
        this.nombres = limpiar(nombres);
        this.apellidos = limpiar(apellidos);
        this.telefono = limpiar(telefono);
        this.correo = limpiar(correo);
        this.direccion = limpiar(direccion);
    }

    private static String limpiar(String texto) {
        return texto == null ? "" : texto.strip();
    }

    public long getIdPropietario() {

        return idPropietario;

    }
    public String getTipoDocumento() {
        return tipoDocumento;
    }
    public String getNumeroDocumento() {
        return numeroDocumento;
    }
    public String getNombres() {
        return nombres;
    }
    public String getApellidos() {
        return apellidos;
    }
    public String getTelefono() {
        return telefono;
    }
    public String getCorreo() {
        return correo;
    }
    public String getDireccion() {
        return direccion;
    }
}
