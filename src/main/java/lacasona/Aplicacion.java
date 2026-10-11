package lacasona;

import java.nio.file.Path;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import lacasona.configuracion.ConexionBaseDatos;
import lacasona.controlador.PropietarioControlador;
import lacasona.repositorio.PropietarioRepositorio;
import lacasona.servicio.AccesoServicio;
import lacasona.servicio.PropietarioServicio;
import lacasona.servicio.Sesion;
import lacasona.vista.PropietarioVentana;

/** Inicia la aplicación de la evidencia GA7-220501096-AA2-EV01. */
public final class Aplicacion {
    private Aplicacion() {
    }

    public static void main(String[] argumentos) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        Path archivo = Path.of(argumentos.length == 0 ? "config/db.properties" : argumentos[0]);
        ConexionBaseDatos baseDatos;
        try {
            baseDatos = new ConexionBaseDatos(archivo);
        } catch (Exception error) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo leer la configuración. Revisa config/db.properties.");
            return;
        }
        AccesoServicio acceso = new AccesoServicio(baseDatos);
        while (true) {
            JTextField usuario = new JTextField("recepcion", 18);
            JPasswordField clave = new JPasswordField(18);
            JPanel ingreso = new JPanel(new java.awt.GridLayout(0, 1, 5, 5));
            ingreso.add(new JLabel("Clínica Veterinaria La Casona"));
            ingreso.add(new JLabel("Usuario"));
            ingreso.add(usuario);
            ingreso.add(new JLabel("Contraseña"));
            ingreso.add(clave);
            if (JOptionPane.showConfirmDialog(null, ingreso, "Iniciar sesión",
                    JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) { return; }
            try {
                Sesion sesion = acceso.ingresar(usuario.getText(), clave.getPassword());
                acceso.comprobarPermiso(sesion, false);
                PropietarioServicio servicio = new PropietarioServicio(
                        new PropietarioRepositorio(baseDatos), acceso, sesion);
                SwingUtilities.invokeLater(() -> new PropietarioVentana(
                        new PropietarioControlador(servicio), sesion).setVisible(true));
                return;
            } catch (IllegalArgumentException | SecurityException error) {
                JOptionPane.showMessageDialog(null, error.getMessage());
            } catch (Exception error) {
                JOptionPane.showMessageDialog(null,
                        "No se pudo iniciar sesión. Revisa la conexión con MySQL.");
            }
        }
    }
}
