package controller;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Locale;
import javax.swing.JOptionPane;
import model.BeneficiosEstudiantilesModel;
import model.BeneficiosModel;
import model.EstudiantesModel;
import pojo.beneficios;
import pojo.beneficiosEstudiantes;
import pojo.estudiante;
import view.DlgAsignarBeneficios;

/**
 * Controlador MVC para el módulo Asignar Beneficios (BeneficiosEstudiantes).
 * Soporta tanto la ruta general desde el menú principal como la ruta directa
 * para un estudiante específico desde FrmEstudianteView.
 *
 * @author jeremydevon
 */
public class BeneficioEstudianteController implements ActionListener {

    private final BeneficiosEstudiantilesModel model;
    private final EstudiantesModel estudiantesModel;
    private final BeneficiosModel beneficiosModel;
    private final DlgAsignarBeneficios vista;
    private boolean cargandoCombos = false;

    /**
     * Constructor que enlaza modelos y el diálogo de asignación de beneficios.
     *
     * @param model modelo de tabla intermedia BeneficiosEstudiantes
     * @param estudiantesModel modelo de estudiantes para cargar lista
     * @param beneficiosModel modelo de beneficios para cargar lista
     * @param vista diálogo de asignación y retiro de beneficios
     */
    public BeneficioEstudianteController(BeneficiosEstudiantilesModel model,
            EstudiantesModel estudiantesModel,
            BeneficiosModel beneficiosModel,
            DlgAsignarBeneficios vista) {
        this.model = model;
        this.estudiantesModel = estudiantesModel;
        this.beneficiosModel = beneficiosModel;
        this.vista = vista;

        this.vista.getBtnAsignar().addActionListener(this);
        this.vista.getBtnQuitar().addActionListener(this);
        this.vista.getCmbEstudiante().addActionListener(e -> {
            if (!cargandoCombos) {
                refrescarBeneficiosEstudianteActual();
            }
        });
    }

    /**
     * Ruta 1 (desde FrmPrincipalView): permite elegir libremente cualquier estudiante existente.
     *
     * @param parent componente padre sobre el cual centrar el diálogo
     */
    public void abrirModoGeneral(Component parent) {
        if (!cargarCombos()) {
            return;
        }
        vista.getCmbEstudiante().setEnabled(true);
        refrescarBeneficiosEstudianteActual();
        vista.setTitle("Asignar Beneficios a Estudiantes");
        vista.setLocationRelativeTo(parent);
        vista.setVisible(true);
    }

    /**
     * Ruta 2 (desde botón Beneficios en FrmEstudianteView): abre el diálogo fijado
     * en el estudiante previamente seleccionado en la tabla de Estudiantes.
     *
     * @param cedulaEstudiante cédula del estudiante seleccionado
     * @param parent componente padre sobre el cual centrar el diálogo
     */
    public void abrirParaEstudiante(int cedulaEstudiante, Component parent) {
        if (!cargarCombos()) {
            return;
        }

        cargandoCombos = true;
        String prefijo = cedulaEstudiante + " - ";
        for (int i = 0; i < vista.getCmbEstudiante().getItemCount(); i++) {
            String item = vista.getCmbEstudiante().getItemAt(i);
            if (item != null && item.startsWith(prefijo)) {
                vista.getCmbEstudiante().setSelectedIndex(i);
                break;
            }
        }
        cargandoCombos = false;

        vista.getCmbEstudiante().setEnabled(false);
        refrescarBeneficiosEstudianteActual();
        vista.setTitle("Beneficios del Estudiante - Cédula " + cedulaEstudiante);
        vista.setLocationRelativeTo(parent);
        vista.setVisible(true);
    }

    private boolean cargarCombos() {
        List<estudiante> listaEstudiantes = estudiantesModel.listarEstudiantesCombo();
        if (listaEstudiantes.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "No hay estudiantes registrados aún. Registre al menos un estudiante primero.",
                    "Sin estudiantes", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        List<beneficios> listaBeneficios = beneficiosModel.listarBeneficiosCombo();
        if (listaBeneficios.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "No hay beneficios registrados aún. Registre al menos un beneficio primero.",
                    "Sin beneficios", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        cargandoCombos = true;
        vista.getCmbEstudiante().removeAllItems();
        for (estudiante est : listaEstudiantes) {
            vista.getCmbEstudiante().addItem(est.toString());
        }

        vista.getCmbBeneficio().removeAllItems();
        for (beneficios ben : listaBeneficios) {
            vista.getCmbBeneficio().addItem(ben.toString());
        }
        cargandoCombos = false;
        return true;
    }

    private int obtenerCedulaSeleccionada() {
        String item = (String) vista.getCmbEstudiante().getSelectedItem();
        if (item == null || !item.contains(" - ")) {
            return -1;
        }
        return Integer.parseInt(item.split(" - ")[0].trim());
    }

    private int obtenerIdBeneficioSeleccionado() {
        String item = (String) vista.getCmbBeneficio().getSelectedItem();
        if (item == null || !item.contains(" - ")) {
            return -1;
        }
        return Integer.parseInt(item.split(" - ")[0].trim());
    }

    private void refrescarBeneficiosEstudianteActual() {
        int cedula = obtenerCedulaSeleccionada();
        if (cedula <= 0) {
            return;
        }
        vista.getTblBeneficiosAsignados().setModel(model.mostrarBeneficiosDeEstudiante(cedula));
        double total = model.obtenerTotalBeneficiosEstudiante(cedula);
        vista.getTxtTotalBeneficios().setText("₡ " + String.format(Locale.US, "%.2f", total));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == vista.getBtnAsignar()) {
            asignarBeneficioSeleccionado();
        } else if (src == vista.getBtnQuitar()) {
            quitarBeneficioSeleccionado();
        }
    }

    private void asignarBeneficioSeleccionado() {
        int cedula = obtenerCedulaSeleccionada();
        int idBeneficio = obtenerIdBeneficioSeleccionado();

        if (cedula <= 0 || idBeneficio <= 0) {
            JOptionPane.showMessageDialog(vista, "Seleccione un estudiante y un beneficio válidos.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (model.yaTieneBeneficio(cedula, idBeneficio)) {
            JOptionPane.showMessageDialog(vista,
                    "El estudiante seleccionado ya cuenta con este beneficio asignado.",
                    "Beneficio ya asignado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        beneficiosEstudiantes nuevaAsignacion = new beneficiosEstudiantes(0, cedula, idBeneficio);
        if (model.asignarBeneficio(nuevaAsignacion)) {
            refrescarBeneficiosEstudianteActual();
            JOptionPane.showMessageDialog(vista, "Beneficio asignado correctamente al estudiante.");
        } else {
            JOptionPane.showMessageDialog(vista, "No se pudo asignar el beneficio.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void quitarBeneficioSeleccionado() {
        int fila = vista.getTblBeneficiosAsignados().getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(vista,
                    "Seleccione en la tabla inferior el beneficio asignado que desea quitar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idBeneficioEstud = Integer.parseInt(
                vista.getTblBeneficiosAsignados().getValueAt(fila, 0).toString());
        String nombreBeneficio = vista.getTblBeneficiosAsignados().getValueAt(fila, 2).toString();

        int confirmar = JOptionPane.showConfirmDialog(vista,
                "¿Desea quitar el beneficio \"" + nombreBeneficio + "\" a este estudiante?",
                "Confirmar retiro", JOptionPane.YES_NO_OPTION);

        if (confirmar == JOptionPane.YES_OPTION) {
            if (model.quitarBeneficio(idBeneficioEstud)) {
                refrescarBeneficiosEstudianteActual();
                JOptionPane.showMessageDialog(vista, "Beneficio retirado correctamente.");
            } else {
                JOptionPane.showMessageDialog(vista, "No se pudo quitar el beneficio seleccionado.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
