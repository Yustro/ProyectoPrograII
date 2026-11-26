package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.BeneficiosModel;
import pojo.beneficios;
import view.FrmBeneficiosView;
import view.FrmNuevosBeneficiosView;

/**
 * Controlador MVC para la gestión CRUD del módulo de Beneficios complementarios.
 *
 * @author jeremydevon
 */
public class BeneficiosController implements ActionListener, KeyListener {

    private final beneficios beneficio;
    private final BeneficiosModel model;
    private final FrmBeneficiosView vista;
    private final FrmNuevosBeneficiosView formulario;
    private int operacion = 1; // 1 = Insertar, 2 = Modificar

    /**
     * Constructor que conecta modelo y vistas del módulo de Beneficios.
     *
     * @param beneficio instancia POJO de beneficios
     * @param model modelo de acceso a datos BeneficiosModel
     * @param vista diálogo principal de listado y filtrado
     * @param formulario ventana de registro y edición
     */
    public BeneficiosController(beneficios beneficio, BeneficiosModel model,
            FrmBeneficiosView vista, FrmNuevosBeneficiosView formulario) {
        this.beneficio = beneficio;
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
     * Abre la ventana de gestión de beneficios recargando los datos.
     *
     * @param parent componente padre para centrar
     */
    public void abrirVista(java.awt.Component parent) {
        vista.txtBuscar.setText("");
        refrescarTabla();
        vista.setLocationRelativeTo(parent);
        vista.setVisible(true);
    }

    /**
     * Recarga el contenido de la tabla según el filtro activo y actualiza el contador.
     */
    public void refrescarTabla() {
        DefaultTableModel modeloTabla = model.filtrarBeneficios(vista.txtBuscar.getText());
        vista.tblBeneficios.setModel(modeloTabla);
        vista.txtRegs.setText(String.valueOf(modeloTabla.getRowCount()));
    }

    private void limpiarFormulario() {
        if (operacion == 1) {
            formulario.getTxtIdBeneficio().setText("");
            formulario.getTxtIdBeneficio().requestFocusInWindow();
        } else {
            formulario.getTxtNombreBeneficio().requestFocusInWindow();
        }
        formulario.getTxtNombreBeneficio().setText("");
        formulario.getTxtDescripcion().setText("");
        formulario.getTxtMonto().setText("");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == vista.btnNuevo) {
            operacion = 1;
            formulario.getTxtIdBeneficio().setEditable(true);
            limpiarFormulario();
            formulario.setTitle("Registrar Nuevo Beneficio");
            formulario.setLocationRelativeTo(vista);
            formulario.setVisible(true);
        } else if (src == vista.btnEditar) {
            cargarSeleccionParaEditar();
        } else if (src == vista.btnEliminar) {
            eliminarSeleccion();
        } else if (src == formulario.getBtnGuardar()) {
            guardarBeneficio();
        } else if (src == formulario.getBtnLimpiar()) {
            limpiarFormulario();
        } else if (src == formulario.getBtnCancelar()) {
            formulario.dispose();
        }
    }

    private void cargarSeleccionParaEditar() {
        int fila = vista.tblBeneficios.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(vista, "Debe seleccionar un beneficio de la tabla para editar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        operacion = 2;
        formulario.getTxtIdBeneficio().setText(vista.tblBeneficios.getValueAt(fila, 0).toString());
        formulario.getTxtIdBeneficio().setEditable(false);
        formulario.getTxtNombreBeneficio().setText(vista.tblBeneficios.getValueAt(fila, 1).toString());
        formulario.getTxtDescripcion().setText(vista.tblBeneficios.getValueAt(fila, 2).toString());
        formulario.getTxtMonto().setText(vista.tblBeneficios.getValueAt(fila, 3).toString());

        formulario.setTitle("Actualizar Beneficio");
        formulario.setLocationRelativeTo(vista);
        formulario.setVisible(true);
    }

    private void eliminarSeleccion() {
        int fila = vista.tblBeneficios.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(vista, "Debe seleccionar un beneficio de la tabla para eliminar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idBeneficio = Integer.parseInt(vista.tblBeneficios.getValueAt(fila, 0).toString());
        String nombre = vista.tblBeneficios.getValueAt(fila, 1).toString();

        int confirmar = JOptionPane.showConfirmDialog(vista,
                "¿Desea eliminar el beneficio \"" + nombre + "\" (ID: " + idBeneficio + ")?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirmar == JOptionPane.YES_OPTION) {
            if (model.eliminarBeneficio(idBeneficio)) {
                JOptionPane.showMessageDialog(vista, "Beneficio eliminado correctamente.");
                refrescarTabla();
            } else {
                JOptionPane.showMessageDialog(vista,
                        "No se pudo eliminar el beneficio porque ya se encuentra asignado a uno o más estudiantes.",
                        "No permitido", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void guardarBeneficio() {
        String idTxt = formulario.getTxtIdBeneficio().getText().trim();
        String nombre = formulario.getTxtNombreBeneficio().getText().trim();
        String descripcion = formulario.getTxtDescripcion().getText().trim();
        String montoTxt = formulario.getTxtMonto().getText().trim().replace(",", ".");

        if (idTxt.isEmpty() || nombre.isEmpty() || descripcion.isEmpty() || montoTxt.isEmpty()) {
            JOptionPane.showMessageDialog(formulario, "Todos los campos del beneficio son obligatorios.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idBeneficio;
        try {
            idBeneficio = Integer.parseInt(idTxt);
            if (idBeneficio <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(formulario, "El ID del beneficio debe ser un número entero positivo.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(montoTxt);
            if (monto <= 0) {
                throw new NumberFormatException();
            }
            monto = Math.round(monto * 100.0) / 100.0;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(formulario, "El monto debe ser un valor numérico positivo (ej. 35000.00).",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        beneficio.setIdBeneficio(idBeneficio);
        beneficio.setNomBeneficio(nombre);
        beneficio.setDescripcion(descripcion);
        beneficio.setMontoBeneficio(monto);

        if (operacion == 1) {
            if (model.existeBeneficio(idBeneficio)) {
                JOptionPane.showMessageDialog(formulario,
                        "Ya existe un beneficio registrado con el ID " + idBeneficio + ".",
                        "ID Duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (model.insertarBeneficio(beneficio)) {
                JOptionPane.showMessageDialog(formulario, "Beneficio registrado exitosamente.");
                refrescarTabla();
                formulario.dispose();
            } else {
                JOptionPane.showMessageDialog(formulario, "Error al guardar el beneficio en la base de datos.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            if (model.modificarBeneficio(beneficio)) {
                JOptionPane.showMessageDialog(formulario, "Beneficio actualizado exitosamente.");
                refrescarTabla();
                formulario.dispose();
            } else {
                JOptionPane.showMessageDialog(formulario, "Error al actualizar el beneficio.",
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
