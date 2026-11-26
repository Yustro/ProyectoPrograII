package controller;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Locale;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import model.PagosMensualesModel;
import view.DlgPlanillasView;
import view.FrmPagosMensualesView;

/**
 * Controlador MVC para la generación, consulta por mes/año y filtrado de Pagos Mensuales.
 *
 * @author jeremydevon
 */
public class PagosMensualesController implements ActionListener, KeyListener {

    private final PagosMensualesModel model;
    private final FrmPagosMensualesView vista;
    private final DlgPlanillasView dlgPlanilla;

    /**
     * Constructor que enlaza el modelo de pagos con la ventana de gestión y el diálogo de planillas.
     *
     * @param model modelo PagosMensualesModel
     * @param vista ventana principal de Pagos Mensuales
     * @param dlgPlanilla diálogo de visualización detallada de planilla mensual
     */
    public PagosMensualesController(PagosMensualesModel model,
            FrmPagosMensualesView vista, DlgPlanillasView dlgPlanilla) {
        this.model = model;
        this.vista = vista;
        this.dlgPlanilla = dlgPlanilla;

        this.vista.getBtnGenerar().addActionListener(this);
        this.vista.getBtnMostrarPlantilla().addActionListener(this);
        this.vista.getChkEstudiante().addActionListener(this);
        this.vista.getChkMes().addActionListener(this);
        this.vista.getChkAnio().addActionListener(this);
        this.vista.getTxtBuscar().addKeyListener(this);

        this.dlgPlanilla.getBtnCerrar().addActionListener(e -> this.dlgPlanilla.dispose());

        this.vista.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                vista.getTxtFechaActual().setText(LocalDate.now().toString());
                aplicarFiltro();
            }
        });
    }

    /**
     * Abre la ventana de Pagos Mensuales cargando la fecha del sistema y todos los registros.
     *
     * @param parent componente padre sobre el cual centrar la ventana
     */
    public void abrirVista(Component parent) {
        vista.getTxtFechaActual().setText(LocalDate.now().toString());
        vista.getTxtBuscar().setText("");
        aplicarFiltro();
        vista.setLocationRelativeTo(parent);
        vista.setVisible(true);
    }

    /**
     * Aplica el filtro de búsqueda según el texto ingresado y los JCheckBox activos.
     */
    public void aplicarFiltro() {
        DefaultTableModel modelo = model.filtrarPagos(
                vista.getTxtBuscar().getText(),
                vista.getChkEstudiante().isSelected(),
                vista.getChkMes().isSelected(),
                vista.getChkAnio().isSelected()
        );
        vista.getTblPagos().setModel(modelo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == vista.getBtnGenerar()) {
            generarPlanilla();
        } else if (src == vista.getBtnMostrarPlantilla()) {
            mostrarPlanillaDelMes();
        } else if (src == vista.getChkEstudiante()
                || src == vista.getChkMes()
                || src == vista.getChkAnio()) {
            aplicarFiltro();
        }
    }

    private void generarPlanilla() {
        String mesTexto = (String) vista.getCmbMesGenerar().getSelectedItem();
        int mesNumero = vista.getCmbMesGenerar().getSelectedIndex() + 1;
        int anio = ((Number) vista.getSpnAnioGenerar().getValue()).intValue();

        LocalDate hoy = LocalDate.now();
        YearMonth periodoActual = YearMonth.from(hoy);
        YearMonth periodoSeleccionado = YearMonth.of(anio, mesNumero);

        // Regla 1: Prohibido generar planillas para un mes/año anterior al actual
        if (periodoSeleccionado.isBefore(periodoActual)) {
            JOptionPane.showMessageDialog(vista,
                    "No se puede generar una planilla para un mes/año anterior al periodo actual ("
                    + hoy.getMonthValue() + "/" + hoy.getYear() + ").",
                    "Periodo inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Regla 2: Prohibido generar más de 1 vez la planilla de un mismo mes/año
        if (model.existePlanillaMesAnio(mesTexto, anio)) {
            JOptionPane.showMessageDialog(vista,
                    "Ya fue generada la planilla correspondiente a " + mesTexto + " " + anio
                    + ".\nNo se permite generar más de una vez el mismo mes/año.",
                    "Planilla duplicada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate fechaPago = periodoSeleccionado.equals(periodoActual)
                ? hoy
                : periodoSeleccionado.atDay(1);

        int cantidadInsertados = model.generarPlanillaMensual(mesTexto, hoy, fechaPago);
        if (cantidadInsertados > 0) {
            aplicarFiltro();
            JOptionPane.showMessageDialog(vista,
                    "Planilla de " + mesTexto + " " + anio + " generada con éxito.\n"
                    + "Estudiantes beneficiados procesados: " + cantidadInsertados,
                    "Planilla generada", JOptionPane.INFORMATION_MESSAGE);
        } else if (cantidadInsertados == 0) {
            JOptionPane.showMessageDialog(vista,
                    "No hay estudiantes con beneficios asignados actualmente para generar la planilla.",
                    "Sin beneficiarios", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(vista,
                    "Ocurrió un error al generar la planilla en la base de datos.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarPlanillaDelMes() {
        String mesTexto = (String) vista.getCmbMesMostrar().getSelectedItem();
        int anio = ((Number) vista.getSpnAnioMostrar().getValue()).intValue();

        DefaultTableModel modeloPlanilla = model.obtenerPlanillaPorMesAnio(mesTexto, anio);
        if (modeloPlanilla.getRowCount() == 0) {
            JOptionPane.showMessageDialog(vista,
                    "No existen pagos registrados para la planilla de " + mesTexto + " " + anio + ".",
                    "Sin registros", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        double[] totales = model.obtenerTotalesPlanillaMesAnio(mesTexto, anio);

        dlgPlanilla.getLblTituloPlanilla().setText("Planilla de beneficios - " + mesTexto + " " + anio);
        dlgPlanilla.setTitle("Planilla Mensual - " + mesTexto + " " + anio);
        dlgPlanilla.getTblPlanillaMes().setModel(modeloPlanilla);
        dlgPlanilla.getTxtTotalBeneficiosPlanilla().setText("₡ " + String.format(Locale.US, "%.2f", totales[0]));
        dlgPlanilla.getTxtTotalDeducciones().setText("₡ " + String.format(Locale.US, "%.2f", totales[1]));
        dlgPlanilla.getTxtTotalNetoPlanilla().setText("₡ " + String.format(Locale.US, "%.2f", totales[2]));

        dlgPlanilla.setLocationRelativeTo(vista);
        dlgPlanilla.setVisible(true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        aplicarFiltro();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }
}
