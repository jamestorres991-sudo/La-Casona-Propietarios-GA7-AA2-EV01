package lacasona.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import lacasona.controlador.PropietarioControlador;
import lacasona.modelo.Propietario;
import lacasona.servicio.Sesion;

/** Formulario para registrar, buscar, actualizar y eliminar propietarios. */
public final class PropietarioVentana extends JFrame {
    private final PropietarioControlador controlador;
    private final Sesion sesion;
    private final JTextField busqueda = new JTextField(18);
    private final JComboBox<String> filtroTipo = new JComboBox<>(
            new String[] {"Todos", "CC", "CE", "TI", "PAS", "PEP", "PPT"});
    private final JTextField[] campos = new JTextField[7];
    private final DefaultTableModel modeloTabla = new DefaultTableModel(
            new String[] {"ID", "Tipo", "Documento", "Nombres", "Apellidos", "Teléfono"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final JLabel estado = new JLabel("Listo para consultar.");
    private final List<JButton> botones = new ArrayList<>();
    private List<Propietario> registros = new ArrayList<>();
    private long idSeleccionado;
    private int pagina;
    private boolean estaTrabajando;

    public PropietarioVentana(PropietarioControlador controlador, Sesion sesion) {
        super("La Casona | Propietarios");
        this.controlador = controlador;
        this.sesion = sesion;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evento) {
                salir();
            }
        });
        JPanel contenido = new JPanel(new BorderLayout(12, 12));
        contenido.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        setContentPane(contenido);
        JPanel encabezado = new JPanel(new GridLayout(2, 1));
        JLabel titulo = new JLabel("Clínica Veterinaria La Casona · Propietarios");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22));
        titulo.setForeground(new Color(20, 85, 77));
        encabezado.add(titulo);
        encabezado.add(new JLabel("Usuario: " + sesion.getNombreUsuario()
                + " | Registro y consulta de propietarios"));
        contenido.add(encabezado, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(12, 12));
        JPanel filtros = new JPanel();
        filtros.add(new JLabel("Documento o nombre:"));
        busqueda.setName("busqueda");
        filtros.add(busqueda);
        filtros.add(filtroTipo);
        filtros.add(boton("Buscar", () -> { pagina = 0; cargar(); }));
        filtros.add(boton("Anterior", () -> { if (pagina > 0) { pagina--; cargar(); } }));
        filtros.add(boton("Siguiente", () -> {
            if (registros.size() == 50) { pagina++; cargar(); }
        }));
        busqueda.addActionListener(evento -> { pagina = 0; cargar(); });
        centro.add(filtros, BorderLayout.NORTH);
        tabla.setRowHeight(28);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting()) { seleccionar(); }
        });
        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        formulario.setBorder(BorderFactory.createTitledBorder("Datos del propietario"));
        String[] etiquetas = {"Tipo de documento *", "Número de documento *", "Nombres *",
                "Apellidos *", "Teléfono *", "Correo", "Dirección"};
        for (int posicion = 0; posicion < campos.length; posicion++) {
            JLabel etiqueta = new JLabel(etiquetas[posicion]);
            campos[posicion] = new JTextField(20);
            campos[posicion].setName("campo" + posicion);
            etiqueta.setLabelFor(campos[posicion]);
            formulario.add(etiqueta);
            formulario.add(campos[posicion]);
        }
        formulario.add(new JLabel("* Campo obligatorio"));
        formulario.add(new JLabel("Correo y dirección son opcionales."));
        JPanel acciones = new JPanel();
        acciones.add(boton("Nuevo", this::limpiar));
        acciones.add(boton("Guardar", this::guardar));
        acciones.add(boton("Eliminar", this::eliminar));
        acciones.add(boton("Cerrar sesión", this::salir));
        JPanel inferior = new JPanel(new BorderLayout());
        inferior.add(formulario, BorderLayout.CENTER);
        inferior.add(acciones, BorderLayout.SOUTH);
        centro.add(inferior, BorderLayout.SOUTH);
        contenido.add(centro, BorderLayout.CENTER);
        contenido.add(estado, BorderLayout.SOUTH);
        setMinimumSize(new Dimension(920, 690));
        setSize(1060, 740);
        setLocationRelativeTo(null);
        limpiar();
        cargar();
    }

    private JButton boton(String nombre, Runnable accion) {
        JButton boton = new JButton(nombre);
        boton.addActionListener(evento -> accion.run());
        botones.add(boton);
        return boton;
    }

    private void cargar() {
        String texto = busqueda.getText();
        String tipo = filtroTipo.getSelectedIndex() == 0 ? ""
                : filtroTipo.getSelectedItem().toString();
        ejecutar(() -> controlador.buscar(texto, tipo, pagina), encontrados -> {
            registros = encontrados;
            modeloTabla.setRowCount(0);
            limpiar();
            for (Propietario propietario : registros) {
                modeloTabla.addRow(new Object[] {propietario.getIdPropietario(),
                    propietario.getTipoDocumento(), propietario.getNumeroDocumento(),
                    propietario.getNombres(), propietario.getApellidos(),
                    propietario.getTelefono()});
            }
            estado.setText("Página " + (pagina + 1) + " | " + registros.size()
                    + " propietarios encontrados.");
        });
    }

    private void seleccionar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || estaTrabajando) {
            return;
        }
        Propietario propietario = registros.get(tabla.convertRowIndexToModel(fila));
        idSeleccionado = propietario.getIdPropietario();
        String[] datos = {propietario.getTipoDocumento(), propietario.getNumeroDocumento(),
            propietario.getNombres(), propietario.getApellidos(), propietario.getTelefono(),
            propietario.getCorreo(), propietario.getDireccion()};
        for (int posicion = 0; posicion < campos.length; posicion++) {
            campos[posicion].setText(datos[posicion]);
        }
        estado.setText("Editando el propietario " + idSeleccionado + ".");
    }

    private void limpiar() {
        idSeleccionado = 0;
        tabla.clearSelection();
        for (JTextField campo : campos) {
            campo.setText("");
        }
        campos[0].setText("CC");
        estado.setText("Nuevo propietario. Completa los campos obligatorios.");
    }

    private void guardar() {
        Propietario propietario = new Propietario(idSeleccionado, campos[0].getText(),
                campos[1].getText(), campos[2].getText(), campos[3].getText(),
                campos[4].getText(), campos[5].getText(), campos[6].getText());
        ejecutar(() -> controlador.guardar(propietario), identificador -> {
            JOptionPane.showMessageDialog(this, "Propietario guardado. ID: " + identificador);
            pagina = 0;
            cargar();
        });
    }

    private void eliminar() {
        if (idSeleccionado == 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un propietario de la tabla.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this,
                "¿Deseas eliminar a " + campos[2].getText() + " " + campos[3].getText()
                + "?\nSolo se permite si no tiene mascotas ni facturas.", "Eliminar propietario",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        long identificador = idSeleccionado;
        ejecutar(() -> {
            controlador.eliminar(identificador);
            return identificador;
        }, resultado -> {
            JOptionPane.showMessageDialog(this, "Propietario eliminado.");
            cargar();
        });
    }

    private void salir() {
        if (estaTrabajando) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "¿Deseas cerrar la sesión?",
                "Cerrar sesión", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            sesion.cerrar();
            dispose();
        }
    }

    private void habilitar(boolean habilitado) {
        estaTrabajando = !habilitado;
        for (JButton boton : botones) {
            boton.setEnabled(habilitado);
        }
        for (JTextField campo : campos) {
            campo.setEnabled(habilitado);
        }
        tabla.setEnabled(habilitado);
        busqueda.setEnabled(habilitado);
        filtroTipo.setEnabled(habilitado);
    }

    private <T> void ejecutar(Callable<T> tarea, Consumer<T> alTerminar) {
        if (estaTrabajando) {
            return;
        }
        habilitar(false);
        estado.setText("Consultando MySQL, espera un momento...");
        // SwingWorker evita que la ventana se quede quieta durante una consulta.
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return tarea.call();
            }

            @Override
            protected void done() {
                habilitar(true);
                try {
                    alTerminar.accept(get());
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    mostrarError(error);
                } catch (ExecutionException error) {
                    mostrarError(error.getCause());
                }
            }
        }.execute();
    }

    private void mostrarError(Throwable error) {
        String mensaje;
        if (error instanceof SQLException sql) {
            if (sql.getErrorCode() == 1062) {
                mensaje = "Ya existe un propietario con ese tipo y número de documento.";
            } else if (sql.getErrorCode() == 1451) {
                mensaje = "No se puede eliminar: el propietario tiene mascotas o facturas.";
            } else {
                mensaje = "No se pudo completar la operación en MySQL. Revisa la conexión. "
                        + "Antes de guardar otra vez, busca el propietario "
                        + "para comprobar el resultado.";
            }
            System.err.println("Error de MySQL: " + sql.getErrorCode()
                    + " / " + sql.getSQLState());
        } else if (error instanceof IllegalArgumentException
                || error instanceof SecurityException) {
            mensaje = error.getMessage();
        } else {
            mensaje = "No se pudo completar la operación. Revisa e intenta nuevamente.";
        }
        estado.setText(mensaje);
        JOptionPane.showMessageDialog(this, mensaje, "Revisar operación",
                JOptionPane.WARNING_MESSAGE);
    }
}
