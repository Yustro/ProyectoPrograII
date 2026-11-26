package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import model.CarrerasModel;
import model.EstudiantesModel;
import model.PagosMensualesModel;
import pojo.carreras;
import pojo.estudiante;
import view.DlgPlanillasView;
import view.FrmCarreraView;
import view.FrmEstudianteView;
import view.FrmNuevaCarreraView;
import view.FrmNuevoEstudianteView;
import view.FrmPagosMensualesView;
import view.FrmPrincipalView;

/**
 * Controlador principal del sistema que coordina los módulos desde FrmPrincipalView.
 *
 * @author jeremydevon
 */
public class MainController implements ActionListener {

    private final FrmPrincipalView frmPrincipal;

    private final CarrerasController carrerasController;
    private final EstudianteController estudianteController;
    private final PagosMensualesController pagosMensualesController;
 
    /**
     * Inicializa los modelos, vistas y controladores activos del sistema MVC.
     *
     * @param frmPrincipal ventana principal JFrame de la aplicación
     */
    public MainController(FrmPrincipalView frmPrincipal) {
        this.frmPrincipal = frmPrincipal;

        // Instancias POJO
        carreras pojoCarrera = new carreras();
        estudiante pojoEstudiante = new estudiante();

        // Instancias Model (Carreras, Estudiantes y Pagos Mensuales)
        CarrerasModel carrerasModel = new CarrerasModel();
        EstudiantesModel estudiantesModel = new EstudiantesModel();
        PagosMensualesModel pagosModel = new PagosMensualesModel();

        // Instancias View
        FrmCarreraView frmCarreras = new FrmCarreraView(frmPrincipal, true);
        FrmNuevaCarreraView frmNuevaCarrera = new FrmNuevaCarreraView();

        FrmEstudianteView frmEstudiantes = new FrmEstudianteView(frmPrincipal, true);
        FrmNuevoEstudianteView frmNuevoEstudiante = new FrmNuevoEstudianteView();

        FrmPagosMensualesView frmPagos = new FrmPagosMensualesView();
        DlgPlanillasView dlgPlanilla = new DlgPlanillasView(frmPagos, true);

        // Instancias Controller
        this.carrerasController = new CarrerasController(
                pojoCarrera, carrerasModel, frmCarreras, frmNuevaCarrera);
        this.estudianteController = new EstudianteController(
                pojoEstudiante, estudiantesModel, carrerasModel,
                frmEstudiantes, frmNuevoEstudiante);
        this.pagosMensualesController = new PagosMensualesController(
                pagosModel, frmPagos, dlgPlanilla);

        // Eventos de botones en el formulario principal
        this.frmPrincipal.getBtnEstudiantes().addActionListener(this);
        this.frmPrincipal.getBtnCarreras().addActionListener(this);
        this.frmPrincipal.getBtnBeneficios().addActionListener(this);
        this.frmPrincipal.getBtnAsignarBeneficios().addActionListener(this);
        this.frmPrincipal.getBtnPagosMensuales().addActionListener(this);

        // Eventos de acceso secundario en la barra de menú (JMenuBar)
        this.frmPrincipal.getMniEstudiantes().addActionListener(this);
        this.frmPrincipal.getMniCarreras().addActionListener(this);
        this.frmPrincipal.getMniBeneficios().addActionListener(this);
        this.frmPrincipal.getMniAsignarBeneficios().addActionListener(this);
        this.frmPrincipal.getMniPagosMensuales().addActionListener(this);
        this.frmPrincipal.getMniSalir().addActionListener(this);

        iniciarVistaPrincipal();
    }

    private void iniciarVistaPrincipal() {
        frmPrincipal.setTitle("UTN - Sistema de Gestión de Beneficios Estudiantiles");
        frmPrincipal.setLocationRelativeTo(null);
        frmPrincipal.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == frmPrincipal.getBtnEstudiantes() || src == frmPrincipal.getMniEstudiantes()) {
            estudianteController.abrirVista(frmPrincipal);
        } else if (src == frmPrincipal.getBtnCarreras() || src == frmPrincipal.getMniCarreras()) {
            carrerasController.abrirVista(frmPrincipal);
        } else if (src == frmPrincipal.getBtnPagosMensuales() || src == frmPrincipal.getMniPagosMensuales()) {
            pagosMensualesController.abrirVista(frmPrincipal);
        } else if (src == frmPrincipal.getBtnBeneficios() || src == frmPrincipal.getMniBeneficios()) {
            // TODO (Compañero - Daniel): Abrir vista desde BeneficiosController
            JOptionPane.showMessageDialog(frmPrincipal, "Módulo de Beneficios pendiente de implementación.");
        } else if (src == frmPrincipal.getBtnAsignarBeneficios() || src == frmPrincipal.getMniAsignarBeneficios()) {
            // TODO (Compañero - Daniel): Abrir diálogo desde BeneficioEstudianteController
            JOptionPane.showMessageDialog(frmPrincipal, "Módulo Asignar Beneficios pendiente de implementación.");
        } else if (src == frmPrincipal.getMniSalir()) {
            int salir = JOptionPane.showConfirmDialog(frmPrincipal,
                    "¿Desea salir del sistema?", "Confirmar salida", JOptionPane.YES_NO_OPTION);
            if (salir == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        }
    }
}
