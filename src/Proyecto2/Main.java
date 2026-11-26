package Proyecto2;

import controller.MainController;
import javax.swing.UIManager;
import view.FrmPrincipalView;

/**
 * Clase principal de arranque del Sistema de Gestión de Beneficios Estudiantiles UTN.
 *
 * @author jeremydevon
 */
public class Main {

    /**
     * Punto de entrada de la aplicación Java Swing con arquitectura MVC.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            System.err.println("No se pudo aplicar el tema Nimbus: " + ex.getMessage());
        }

        java.awt.EventQueue.invokeLater(() -> {
            FrmPrincipalView frmPrincipal = new FrmPrincipalView();
            new MainController(frmPrincipal);
        });
    }
}
