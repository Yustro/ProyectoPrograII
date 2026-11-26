package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import model.CarrerasModel;
import model.EstudiantesModel;
import pojo.carreras;
import pojo.estudiante;
import view.FrmEstudianteView;
import view.FrmNuevoEstudianteView;

/**
 * Controlador MVC para la gestión CRUD del módulo de Estudiantes y acceso rápido a sus beneficios.
 *
 * @author jeremydevon
 */
public class EstudianteController implements ActionListener, KeyListener {

    private final estudiante est;
    private final EstudiantesModel model;
    private final CarrerasModel carrerasModel;
    private final FrmEstudianteView vista;
    private final FrmNuevoEstudianteView formulario;
    private int operacion = 1; // 1 = Insertar, 2 = Modificar

    /**
     * Constructor que conecta modelos y vistas del módulo de Estudiantes.
     *
     * @param est instancia POJO de estudiante
     * @param model modelo de acceso a datos EstudiantesModel
     * @param carrerasModel modelo de carreras para llenar el JComboBox
     * @param vista diálogo principal de listado y búsqueda
     * @param formulario ventana de registro y edición
     */
    public EstudianteController(estudiante est, EstudiantesModel model, CarrerasModel carrerasModel,
            FrmEstudianteView vista, FrmNuevoEstudianteView formulario) {
        this.est = est;
        this.model = model;
        this.carrerasModel = carrerasModel;
        this.vista = vista;
        this.formulario = formulario;

        configurarMascaraTelefono();

        this.vista.btnNuevo.addActionListener(this);
        this.vista.btnEditar.addActionListener(this);
        this.vista.btnEliminar.addActionListener(this);
        this.vista.btnBeneficios.addActionListener(this);
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
     * Aplica máscara automática de entrada ####-#### al campo de teléfono.
     */
    private void configurarMascaraTelefono() {
        if (formulario.getTxtTelefono().getDocument() instanceof AbstractDocument doc) {
            doc.setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                        throws BadLocationException {
                    replace(fb, offset, 0, string, attr);
                }

                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                        throws BadLocationException {
                    String actual = fb.getDocument().getText(0, fb.getDocument().getLength());
                    String nuevo = actual.substring(0, offset)
                            + (text == null ? "" : text)
                            + actual.substring(offset + length);
                    String soloDigitos = nuevo.replaceAll("\\D", "");
                    if (soloDigitos.length() > 8) {
                        soloDigitos = soloDigitos.substring(0, 8);
                    }
                    String formateado = soloDigitos.length() > 4
                            ? soloDigitos.substring(0, 4) + "-" + soloDigitos.substring(4)
                            : soloDigitos;
                    fb.replace(0, fb.getDocument().getLength(), formateado, attrs);
                }
            });
        }
    }

    /**
     * Abre la ventana de gestión de estudiantes mostrando los registros actuales.
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
     * Actualiza la tabla de estudiantes y el contador de filas.
     */
    public void refrescarTabla() {
        DefaultTableModel modeloTabla = model.filtrarEstudiantes(vista.txtBuscar.getText());
        vista.tblEstudiantes.setModel(modeloTabla);
        vista.txtRegs.setText(String.valueOf(modeloTabla.getRowCount()));
    }

    private boolean cargarComboCarreras() {
        List<carreras> lista = carrerasModel.listarCarrerasCombo();
        formulario.getCmbCarrera().removeAllItems();
        for (carreras c : lista) {
            formulario.getCmbCarrera().addItem(c.toString());
        }
        if (lista.isEmpty()) {
            JOptionPane.showMessageDialog(vista,
                    "Debe registrar al menos una Carrera antes de gestionar Estudiantes.",
                    "Sin carreras registradas", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void limpiarFormulario() {
        if (operacion == 1) {
            formulario.getTxtCarnet().setText("");
            formulario.getTxtCarnet().requestFocusInWindow();
        } else {
            formulario.getTxtCedula().requestFocusInWindow();
        }
        formulario.getTxtCedula().setText("");
        formulario.getTxtNombreCompleto().setText("");
        formulario.getTxtDireccion().setText("");
        formulario.getTxtTelefono().setText("");
        formulario.getTxtCorreoElectronico().setText("");
        formulario.getDtpFechaNac().clear();
        formulario.getDptFechaIngreso().clear();
        formulario.getDtpFechaEgreso().clear();
        if (formulario.getCmbCarrera().getItemCount() > 0) {
            formulario.getCmbCarrera().setSelectedIndex(0);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == vista.btnNuevo) {
            if (!cargarComboCarreras()) {
                return;
            }
            operacion = 1;
            formulario.getTxtCarnet().setEditable(true);
            limpiarFormulario();
            formulario.setTitle("Registrar Nuevo Estudiante");
            formulario.setLocationRelativeTo(vista);
            formulario.setVisible(true);
        } else if (src == vista.btnEditar) {
            cargarSeleccionParaEditar();
        } else if (src == vista.btnEliminar) {
            eliminarSeleccion();
        } else if (src == vista.btnBeneficios) {
            abrirBeneficiosEstudianteSeleccionado();
        } else if (src == formulario.getBtnGuardar()) {
            guardarEstudiante();
        } else if (src == formulario.getBtnLimpiar()) {
            limpiarFormulario();
        } else if (src == formulario.getBtnCancelar()) {
            formulario.dispose();
        }
    }

    private void cargarSeleccionParaEditar() {
        int fila = vista.tblEstudiantes.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(vista, "Seleccione un estudiante de la tabla para editar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!cargarComboCarreras()) {
            return;
        }

        operacion = 2;
        formulario.getTxtCarnet().setText(vista.tblEstudiantes.getValueAt(fila, 0).toString());
        formulario.getTxtCarnet().setEditable(false);
        formulario.getTxtCedula().setText(vista.tblEstudiantes.getValueAt(fila, 1).toString());
        formulario.getTxtNombreCompleto().setText(vista.tblEstudiantes.getValueAt(fila, 2).toString());
        formulario.getDtpFechaNac().setDate(LocalDate.parse(vista.tblEstudiantes.getValueAt(fila, 3).toString()));
        formulario.getTxtDireccion().setText(vista.tblEstudiantes.getValueAt(fila, 4).toString());
        formulario.getTxtTelefono().setText(vista.tblEstudiantes.getValueAt(fila, 5).toString());
        formulario.getTxtCorreoElectronico().setText(vista.tblEstudiantes.getValueAt(fila, 6).toString());
        formulario.getDptFechaIngreso().setDate(LocalDate.parse(vista.tblEstudiantes.getValueAt(fila, 7).toString()));

        String egresoStr = vista.tblEstudiantes.getValueAt(fila, 8).toString().trim();
        if (egresoStr.isEmpty()) {
            formulario.getDtpFechaEgreso().clear();
        } else {
            formulario.getDtpFechaEgreso().setDate(LocalDate.parse(egresoStr));
        }

        String carreraTabla = vista.tblEstudiantes.getValueAt(fila, 9).toString();
        formulario.getCmbCarrera().setSelectedItem(carreraTabla);

        formulario.setTitle("Actualizar Estudiante");
        formulario.setLocationRelativeTo(vista);
        formulario.setVisible(true);
    }

    private void eliminarSeleccion() {
        int fila = vista.tblEstudiantes.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(vista, "Seleccione un estudiante de la tabla para eliminar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String carnet = vista.tblEstudiantes.getValueAt(fila, 0).toString();
        String nombre = vista.tblEstudiantes.getValueAt(fila, 2).toString();

        int confirmar = JOptionPane.showConfirmDialog(vista,
                "¿Desea eliminar al estudiante \"" + nombre + "\" (Carnet: " + carnet + ")?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirmar == JOptionPane.YES_OPTION) {
            if (model.eliminarEstudiante(carnet)) {
                JOptionPane.showMessageDialog(vista, "Estudiante eliminado correctamente.");
                refrescarTabla();
            } else {
                JOptionPane.showMessageDialog(vista,
                        "No se pudo eliminar al estudiante porque ya posee registros de pagos mensuales asociados.",
                        "No permitido", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void abrirBeneficiosEstudianteSeleccionado() {
        int fila = vista.tblEstudiantes.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(vista,
                    "Seleccione primero un estudiante en la tabla para gestionar sus beneficios.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // TODO (Compañero - Daniel): Conectar aquí con BeneficioEstudianteController pasando la cédula seleccionada
        JOptionPane.showMessageDialog(vista,
                "Módulo Asignar Beneficios pendiente de implementación.",
                "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void guardarEstudiante() {
        String carnet = formulario.getTxtCarnet().getText().trim();
        String cedulaTxt = formulario.getTxtCedula().getText().trim();
        String nombre = formulario.getTxtNombreCompleto().getText().trim();
        String direccion = formulario.getTxtDireccion().getText().trim();
        String telefono = formulario.getTxtTelefono().getText().trim();
        String email = formulario.getTxtCorreoElectronico().getText().trim();
        String carreraItem = (String) formulario.getCmbCarrera().getSelectedItem();

        if (carnet.isEmpty() || cedulaTxt.isEmpty() || nombre.isEmpty()
                || direccion.isEmpty() || telefono.isEmpty() || email.isEmpty() || carreraItem == null) {
            JOptionPane.showMessageDialog(formulario,
                    "Todos los campos (excepto Fecha de Egreso) son obligatorios.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int cedula;
        try {
            cedula = Integer.parseInt(cedulaTxt);
            if (cedula <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(formulario,
                    "La cédula debe ser un número entero positivo válido (sin guiones).",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!telefono.matches("\\d{4}-\\d{4}")) {
            JOptionPane.showMessageDialog(formulario,
                    "El teléfono debe contener 8 dígitos con formato ####-####.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            JOptionPane.showMessageDialog(formulario,
                    "Ingrese una dirección de correo electrónico válida (ejemplo@utn.ac.cr).",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!formulario.getDtpFechaNac().isTextFieldValid()
                || formulario.getDtpFechaNac().getDate() == null) {
            JOptionPane.showMessageDialog(formulario, "Seleccione una fecha de nacimiento válida.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LocalDate fechaNac = formulario.getDtpFechaNac().getDate();
        if (fechaNac.isAfter(LocalDate.now())) {
            JOptionPane.showMessageDialog(formulario, "La fecha de nacimiento no puede ser futura.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!formulario.getDptFechaIngreso().isTextFieldValid()
                || formulario.getDptFechaIngreso().getDate() == null) {
            JOptionPane.showMessageDialog(formulario, "Seleccione una fecha de ingreso válida.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LocalDate fechaIngreso = formulario.getDptFechaIngreso().getDate();
        if (!fechaIngreso.isAfter(fechaNac)) {
            JOptionPane.showMessageDialog(formulario,
                    "La fecha de ingreso debe ser posterior a la fecha de nacimiento.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!formulario.getDtpFechaEgreso().isTextFieldValid()) {
            JOptionPane.showMessageDialog(formulario,
                    "El formato de la fecha de egreso no es válido (o déjela en blanco).",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }
        LocalDate fechaEgreso = formulario.getDtpFechaEgreso().getDate();
        if (fechaEgreso != null && fechaEgreso.isBefore(fechaIngreso)) {
            JOptionPane.showMessageDialog(formulario,
                    "La fecha de egreso no puede ser anterior a la fecha de ingreso.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String excluirCarnet = (operacion == 2) ? carnet : "";
        if (operacion == 1 && model.existeCarnet(carnet)) {
            JOptionPane.showMessageDialog(formulario,
                    "Ya existe un estudiante registrado con el carnet \"" + carnet + "\".",
                    "Carnet Duplicado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (model.existeCedula(cedula, excluirCarnet)) {
            JOptionPane.showMessageDialog(formulario,
                    "Ya existe otro estudiante registrado con la cédula " + cedula + ".",
                    "Cédula Duplicada", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (model.existeEmail(email, excluirCarnet)) {
            JOptionPane.showMessageDialog(formulario,
                    "Ya existe otro estudiante registrado con el correo \"" + email + "\".",
                    "Correo Duplicado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idCarrera = Integer.parseInt(carreraItem.split(" - ")[0].trim());

        est.setCarnet(carnet);
        est.setCedula(cedula);
        est.setNombre(nombre);
        est.setFechNac(fechaNac);
        est.setDireccion(direccion);
        est.setTelefono(telefono);
        est.setEmail(email);
        est.setFechIngreso(fechaIngreso);
        est.setFechEgreso(fechaEgreso);
        est.setIdCarrera(idCarrera);

        if (operacion == 1) {
            if (model.insertarEstudiante(est)) {
                JOptionPane.showMessageDialog(formulario, "Estudiante registrado exitosamente.");
                refrescarTabla();
                formulario.dispose();
            } else {
                JOptionPane.showMessageDialog(formulario, "Error al registrar el estudiante en la base de datos.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            if (model.modificarEstudiante(est)) {
                JOptionPane.showMessageDialog(formulario, "Estudiante actualizado exitosamente.");
                refrescarTabla();
                formulario.dispose();
            } else {
                JOptionPane.showMessageDialog(formulario, "Error al actualizar el estudiante.",
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
