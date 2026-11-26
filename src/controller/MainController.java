package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import model.BeneficiosEstudiantilesModel;
import model.BeneficiosModel;
import model.CarrerasModel;
import model.EstudiantesModel;
import model.PagosMensualesModel;
import pojo.beneficios;
import pojo.carreras;
import pojo.estudiante;
import view.DlgAsignarBeneficios;
import view.DlgPlanillasView;
import view.FrmBeneficiosView;
import view.FrmCarreraView;
import view.FrmEstudianteView;
import view.FrmNuevaCarreraView;
import view.FrmNuevoEstudianteView;
import view.FrmNuevosBeneficiosView;
import view.FrmPagosMensualesView;
import view.FrmPrincipalView;

/**
 * Controlador principal del sistema que coordina todos los módulos desde FrmPrincipalView,
 * enlazando tanto los botones principales como la barra de menú secundaria (JMenuBar).
 *
 * @author jeremydevon
 */
public class MainController implements ActionListener {

    private final FrmPrincipalView frmPrincipal;

    private final CarrerasController carrerasController;
    private final BeneficiosController beneficiosController;
    private final BeneficioEstudianteController beneficioEstudianteController;
    private final EstudianteController estudianteController;
    private final PagosMensualesController pagosMensualesController;

    /**
     * Inicializa todos los modelos, vistas y controladores secundarios del sistema MVC.
     *
     * @param frmPrincipal ventana principal JFrame de la aplicación
     */
    public MainController(FrmPrincipalView frmPrincipal) {
        this.frmPrincipal = frmPrincipal;

        // Instancias POJO
        carreras pojoCarrera = new carreras();
        beneficios pojoBeneficio = new beneficios();
        estudiante pojoEstudiante = new estudiante();

        // Instancias Model
        CarrerasModel carrerasModel = new CarrerasModel();
        BeneficiosModel beneficiosModel = new BeneficiosModel();
        EstudiantesModel estudiantesModel = new EstudiantesModel();
        BeneficiosEstudiantilesModel benEstModel = new BeneficiosEstudiantilesModel();
        PagosMensualesModel pagosModel = new PagosMensualesModel();

        // Instancias View
        FrmCarreraView frmCarreras = new FrmCarreraView(frmPrincipal, true);
        FrmNuevaCarreraView frmNuevaCarrera = new FrmNuevaCarreraView();

        FrmBeneficiosView frmBeneficios = new FrmBeneficiosView(frmPrincipal, true);
        FrmNuevosBeneficiosView frmNuevoBeneficio = new FrmNuevosBeneficiosView();

        DlgAsignarBeneficios dlgAsignar = new DlgAsignarBeneficios(frmPrincipal, true);

        FrmEstudianteView frmEstudiantes = new FrmEstudianteView(frmPrincipal, true);
        FrmNuevoEstudianteView frmNuevoEstudiante = new FrmNuevoEstudianteView();

        FrmPagosMensualesView frmPagos = new FrmPagosMensualesView();
        DlgPlanillasView dlgPlanilla = new DlgPlanillasView(frmPagos, true);

        // Instancias Controller
        this.carrerasController = new CarrerasController(
                pojoCarrera, carrerasModel, frmCarreras, frmNuevaCarrera);
        this.beneficiosController = new BeneficiosController(
                pojoBeneficio, beneficiosModel, frmBeneficios, frmNuevoBeneficio);
        this.beneficioEstudianteController = new BeneficioEstudianteController(
                benEstModel, estudiantesModel, beneficiosModel, dlgAsignar);
        this.estudianteController = new EstudianteController(
                pojoEstudiante, estudiantesModel, carrerasModel,
                frmEstudiantes, frmNuevoEstudiante, beneficioEstudianteController);
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
        } else if (src == frmPrincipal.getBtnBeneficios() || src == frmPrincipal.getMniBeneficios()) {
            beneficiosController.abrirVista(frmPrincipal);
        } else if (src == frmPrincipal.getBtnAsignarBeneficios() || src == frmPrincipal.getMniAsignarBeneficios()) {
            beneficioEstudianteController.abrirModoGeneral(frmPrincipal);
        } else if (src == frmPrincipal.getBtnPagosMensuales() || src == frmPrincipal.getMniPagosMensuales()) {
            pagosMensualesController.abrirVista(frmPrincipal);
        } else if (src == frmPrincipal.getMniSalir()) {
            int salir = JOptionPane.showConfirmDialog(frmPrincipal,
                    "¿Desea salir del sistema?", "Confirmar salida", JOptionPane.YES_NO_OPTION);
            if (salir == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        }
    }
}
