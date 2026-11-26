package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.CarrerasModel;
import pojo.carreras;
import view.FrmCarreraView;
import view.FrmNuevaCarreraView;

/**
 * Controlador MVC para la gestión CRUD del módulo de Carreras.
 *
 * @author jeremydevon
 */
public class CarrerasController implements ActionListener, KeyListener {

    private final carreras carrera;
    private final CarrerasModel model;
    private final FrmCarreraView vista;
    private final FrmNuevaCarreraView formulario;
    private int operacion = 1; // 1 = Insertar, 2 = Modificar

    /**
     * Constructor que enlaza el modelo y las vistas de Carreras.
     *
     * @param carrera instancia POJO de carreras
     * @param model modelo de acceso a datos CarrerasModel
     * @param vista diálogo principal de listado y búsqueda
     * @param formulario ventana de registro y edición
     */
    public CarrerasController(carreras carrera, CarrerasModel model,
            FrmCarreraView vista, FrmNuevaCarreraView formulario) {
        this.carrera = carrera;
        this.model = model;
        this.vista = vista;
        this.formulario = formulario;

        this.vista.btnNuevo.addActionListener(this);
        this.vista.btnEditar.addActionListener(this);
        this.vista.btnEliminar.addActionListener(this);
        this.vista.txtBuscar.addKeyListener(this);

        this.formulario.getBtnGuardar().addActionListener(this);
        this.formulario.getBtnLimpiar().addActionListener(this);
        this.formulario.getBtnCancelar().addActionListener(this);

        this.vista.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                refrescarTabla();
            }
        });
    }

    /**
     * Abre la ventana principal de gestión de carreras cargando sus registros.
     *
     * @param parent componente padre para centrar el diálogo
     */
    public void abrirVista(java.awt.Component parent) {
        vista.txtBuscar.setText("");
        refrescarTabla();
        vista.setLocationRelativeTo(parent);
        vista.setVisible(true);
    }

    /**
     * Actualiza la tabla de carreras y el contador de registros según el filtro actual.
     */
    public void refrescarTabla() {
        DefaultTableModel modeloTabla = model.filtrarCarreras(vista.txtBuscar.getText());
        vista.tblCarreras.setModel(modeloTabla);
        vista.txtRegs.setText(String.valueOf(modeloTabla.getRowCount()));
    }

    private void limpiarFormulario() {
        if (operacion == 1) {
            formulario.getTxtIdCarrera().setText("");
            formulario.getTxtIdCarrera().requestFocusInWindow();
        } else {
            formulario.getTxtNomCarrera().requestFocusInWindow();
        }
        formulario.getTxtNomCarrera().setText("");
        formulario.getCmbGrado().setSelectedIndex(0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == vista.btnNuevo) {
            operacion = 1;
            formulario.getTxtIdCarrera().setEditable(true);
            limpiarFormulario();
            formulario.setTitle("Registrar Nueva Carrera");
            formulario.setLocationRelativeTo(vista);
            formulario.setVisible(true);
        } else if (src == vista.btnEditar) {
            cargarSeleccionParaEditar();
        } else if (src == vista.btnEliminar) {
            eliminarSeleccion();
        } else if (src == formulario.getBtnGuardar()) {
            guardarCarrera();
        } else if (src == formulario.getBtnLimpiar()) {
            limpiarFormulario();
        } else if (src == formulario.getBtnCancelar()) {
            formulario.dispose();
        }
    }

    private void cargarSeleccionParaEditar() {
        int fila = vista.tblCarreras.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(vista, "Debe seleccionar una carrera de la tabla para editar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        operacion = 2;
        formulario.getTxtIdCarrera().setText(vista.tblCarreras.getValueAt(fila, 0).toString());
        formulario.getTxtIdCarrera().setEditable(false);
        formulario.getTxtNomCarrera().setText(vista.tblCarreras.getValueAt(fila, 1).toString());
        formulario.getCmbGrado().setSelectedItem(vista.tblCarreras.getValueAt(fila, 2).toString());

        formulario.setTitle("Actualizar Carrera");
        formulario.setLocationRelativeTo(vista);
        formulario.setVisible(true);
    }

    private void eliminarSeleccion() {
        int fila = vista.tblCarreras.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(vista, "Debe seleccionar una carrera de la tabla para eliminar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idCarrera = Integer.parseInt(vista.tblCarreras.getValueAt(fila, 0).toString());
        String nombre = vista.tblCarreras.getValueAt(fila, 1).toString();

        int confirmar = JOptionPane.showConfirmDialog(vista,
                "¿Desea eliminar la carrera \"" + nombre + "\" (ID: " + idCarrera + ")?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirmar == JOptionPane.YES_OPTION) {
            if (model.eliminarCarrera(idCarrera)) {
                JOptionPane.showMessageDialog(vista, "Carrera eliminada correctamente.");
                refrescarTabla();
            } else {
                JOptionPane.showMessageDialog(vista,
                        "No se pudo eliminar la carrera. Verifique que no tenga estudiantes matriculados asociados.",
                        "No permitido", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void guardarCarrera() {
        String idTxt = formulario.getTxtIdCarrera().getText().trim();
        String nombre = formulario.getTxtNomCarrera().getText().trim();
        String grado = (String) formulario.getCmbGrado().getSelectedItem();

        if (idTxt.isEmpty() || nombre.isEmpty() || grado == null) {
            JOptionPane.showMessageDialog(formulario, "Todos los campos son obligatorios.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idCarrera;
        try {
            idCarrera = Integer.parseInt(idTxt);
            if (idCarrera <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(formulario, "El ID de la carrera debe ser un número entero positivo.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        carrera.setIdCarrera(idCarrera);
        carrera.setNomCarrera(nombre);
        carrera.setGrado(grado);

        if (operacion == 1) {
            if (model.existeCarrera(idCarrera)) {
                JOptionPane.showMessageDialog(formulario,
                        "Ya existe una carrera registrada con el ID " + idCarrera + ".",
                        "ID Duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (model.insertarCarrera(carrera)) {
                JOptionPane.showMessageDialog(formulario, "Carrera registrada exitosamente.");
                refrescarTabla();
                formulario.dispose();
            } else {
                JOptionPane.showMessageDialog(formulario, "Error al guardar la carrera en la base de datos.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            if (model.modificarCarrera(carrera)) {
                JOptionPane.showMessageDialog(formulario, "Carrera actualizada exitosamente.");
                refrescarTabla();
                formulario.dispose();
            } else {
                JOptionPane.showMessageDialog(formulario, "Error al actualizar la carrera.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        refrescarTabla();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }
}
