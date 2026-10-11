package lacasona;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import lacasona.configuracion.ConexionBaseDatos;
import lacasona.controlador.PropietarioControlador;
import lacasona.servicio.AccesoServicio;
import lacasona.servicio.PropietarioServicio;
import lacasona.repositorio.PropietarioRepositorio;
import lacasona.vista.PropietarioVentana;

/** Revisa los botones del formulario con los componentes reales de Swing y JDBC. */
public final class PruebasInterfaz {
    private static PropietarioVentana ventana;
    private static PropietarioServicio servicio;
    private static final List<String> RESULTADOS = new ArrayList<>();

    private PruebasInterfaz() {
    }

    public static void main(String[] argumentos) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        ConexionBaseDatos base = new ConexionBaseDatos(Path.of(argumentos[0]));
        AccesoServicio acceso = new AccesoServicio(base);
        var sesion = acceso.ingresar("recepcion", "Casona2026!".toCharArray());
        servicio = new PropietarioServicio(new PropietarioRepositorio(base), acceso, sesion);
        RESULTADOS.add("Caso;Comprobación;Observado;Resultado");
        try {
            enVentana(() -> {
                ventana = new PropietarioVentana(new PropietarioControlador(servicio), sesion);
                ventana.setVisible(true);
                return null;
            });
            esperarListo();
            comprobar("UI-01", "Carga inicial de la tabla", tabla().getRowCount() > 0);
            enVentana(() -> { boton("Nuevo").doClick(); return null; });
            completar("3000012345", "Ana María", "Gómez Ramírez", "3009876543");
            capturar("01_formulario.png");
            enVentana(() -> { boton("Guardar").doClick(); return null; });
            esperarMensaje("Propietario guardado");
            cerrarMensaje();
            esperarListo();
            comprobar("UI-02", "Botón Guardar crea el propietario",
                    servicio.consultar("3000012345", "CC", 0).size() == 1);
            buscar("3000012345");
            comprobar("UI-03", "Botón Buscar muestra el propietario", tabla().getRowCount() == 1);
            enVentana(() -> { tabla().setRowSelectionInterval(0, 0); return null; });
            comprobar("UI-04", "Seleccionar una fila llena el formulario",
                    enVentana(() -> campo("campo2").getText().equals("Ana María")));
            enVentana(() -> { campo("campo4").setText("3112345678"); return null; });
            enVentana(() -> { boton("Guardar").doClick(); return null; });
            esperarMensaje("Propietario guardado");
            cerrarMensaje();
            esperarListo();
            comprobar("UI-05", "Botón Guardar actualiza el teléfono",
                    servicio.consultar("3000012345", "CC", 0).get(0)
                            .getTelefono().equals("3112345678"));
            enVentana(() -> { boton("Nuevo").doClick(); return null; });
            completar("3000012345", "Ana María", "Gómez Ramírez", "3009876543");
            enVentana(() -> { boton("Guardar").doClick(); return null; });
            esperarMensaje("Ya existe un propietario");
            capturar("02_duplicado.png");
            cerrarMensaje();
            comprobar("UI-06", "Intentar duplicar muestra el aviso y conserva un registro",
                    servicio.consultar("3000012345", "CC", 0).size() == 1);
            buscar("3000012345");
            enVentana(() -> { tabla().setRowSelectionInterval(0, 0); return null; });
            enVentana(() -> { boton("Eliminar").doClick(); return null; }, false);
            esperarMensaje("¿Deseas eliminar");
            responder("Yes");
            esperarMensaje("Propietario eliminado");
            cerrarMensaje();
            esperarListo();
            comprobar("UI-07", "Botón Eliminar borra después de confirmar",
                    servicio.consultar("3000012345", "CC", 0).isEmpty());
            buscar("0012345678");
            enVentana(() -> { tabla().setRowSelectionInterval(0, 0); return null; });
            enVentana(() -> { boton("Eliminar").doClick(); return null; }, false);
            esperarMensaje("¿Deseas eliminar");
            responder("Yes");
            esperarMensaje("No se puede eliminar");
            capturar("03_eliminacion_protegida.png");
            cerrarMensaje();
            comprobar("UI-08", "El formulario informa que el propietario tiene relaciones",
                    servicio.consultar("0012345678", "CC", 0).size() == 1);
            capturar("04_consulta.png");
            Files.write(Path.of("evidencias/pruebas_interfaz.csv"), RESULTADOS,
                    StandardCharsets.UTF_8);
            System.out.println("Pruebas de interfaz: 8. Fallidas: 0.");
        } finally {
            enVentana(() -> {
                for (Window abierta : Window.getWindows()) { abierta.dispose(); }
                return null;
            });
        }
    }

    private static void buscar(String documento) throws Exception {
        enVentana(() -> { campo("busqueda").setText(documento); return null; });
        enVentana(() -> { boton("Buscar").doClick(); return null; });
        esperarListo();
    }

    private static void completar(String documento, String nombres, String apellidos,
            String telefono) throws Exception {
        enVentana(() -> {
            campo("campo0").setText("CC");
            campo("campo1").setText(documento);
            campo("campo2").setText(nombres);
            campo("campo3").setText(apellidos);
            campo("campo4").setText(telefono);
            return null;
        });
    }

    private static void esperarListo() throws Exception {
        for (int intento = 0; intento < 100; intento++) {
            if (enVentana(() -> boton("Buscar").isEnabled())) { return; }
            Thread.sleep(100);
        }
        throw new AssertionError("La ventana no terminó la operación.");
    }

    private static void esperarMensaje(String texto) throws Exception {
        for (int intento = 0; intento < 100; intento++) {
            if (enVentana(() -> {
                for (Window abierta : Window.getWindows()) {
                    if (abierta instanceof JDialog && abierta.isShowing()) {
                        for (Component componente : componentes(abierta)) {
                            if (componente instanceof JOptionPane aviso
                                    && String.valueOf(aviso.getMessage()).contains(texto)) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            })) { return; }
            Thread.sleep(100);
        }
        throw new AssertionError("No apareció el mensaje: " + texto);
    }

    private static void cerrarMensaje() throws Exception {
        responder("OK");
    }

    private static void responder(String etiqueta) throws Exception {
        enVentana(() -> {
            for (Window abierta : Window.getWindows()) {
                if (abierta instanceof JDialog && abierta.isShowing()) {
                    for (Component componente : componentes(abierta)) {
                        if (componente instanceof JButton boton
                                && (boton.getText().equals(etiqueta)
                                || (etiqueta.equals("Yes") && boton.getText().equals("Sí"))
                                || (etiqueta.equals("OK") && boton.getText().equals("Aceptar")))) {
                            boton.doClick();
                            return null;
                        }
                    }
                }
            }
            throw new AssertionError("No se encontró el botón del aviso.");
        });
    }

    private static JButton boton(String texto) {
        return componentes(ventana).stream().filter(c -> c instanceof JButton b
                && b.getText().equals(texto)).map(c -> (JButton) c).findFirst().orElseThrow();
    }

    private static JTextField campo(String nombre) {
        return componentes(ventana).stream().filter(c -> c instanceof JTextField
                && nombre.equals(c.getName())).map(c -> (JTextField) c).findFirst().orElseThrow();
    }

    private static JTable tabla() {
        return componentes(ventana).stream().filter(c -> c instanceof JTable)
                .map(c -> (JTable) c).findFirst().orElseThrow();
    }

    private static List<Component> componentes(Component raiz) {
        List<Component> encontrados = new ArrayList<>();
        encontrados.add(raiz);
        if (raiz instanceof Container contenedor) {
            for (Component hijo : contenedor.getComponents()) {
                encontrados.addAll(componentes(hijo));
            }
        }
        return encontrados;
    }

    private static <T> T enVentana(Callable<T> accion) throws Exception {
        return enVentana(accion, true);
    }

    private static <T> T enVentana(Callable<T> accion, boolean esperar) throws Exception {
        AtomicReference<T> resultado = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();
        Runnable tarea = () -> {
            try { resultado.set(accion.call()); } catch (Exception fallo) { error.set(fallo); }
        };
        if (esperar) { SwingUtilities.invokeAndWait(tarea); }
        else { SwingUtilities.invokeLater(tarea); }
        if (error.get() != null) { throw error.get(); }
        return resultado.get();
    }

    private static void capturar(String nombre) throws Exception {
        enVentana(() -> {
            Files.createDirectories(Path.of("evidencias/vistas"));
            BufferedImage imagen = new BufferedImage(ventana.getWidth(), ventana.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D dibujo = imagen.createGraphics();
            ventana.printAll(dibujo);
            dibujo.dispose();
            ImageIO.write(imagen, "png", Path.of("evidencias/vistas", nombre).toFile());
            return null;
        });
    }

    private static void comprobar(String codigo, String descripcion, boolean cumple) {
        if (!cumple) { throw new AssertionError(descripcion); }
        RESULTADOS.add(codigo + ";" + descripcion + ";Comportamiento comprobado;APROBADO");
        System.out.println(codigo + " APROBADO: " + descripcion);
    }
}
