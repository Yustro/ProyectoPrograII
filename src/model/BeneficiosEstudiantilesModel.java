package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import javax.swing.table.DefaultTableModel;
import pojo.beneficiosEstudiantes;

/**
 * Modelo encargado de gestionar la tabla intermedia BeneficiosEstudiantes.
 *
 * @author jeremydevon
 */
public class BeneficiosEstudiantilesModel {

    /**
     * Verifica si un estudiante ya tiene asignado ese mismo beneficio para evitar duplicados innecesarios.
     *
     * @param cedula cédula del estudiante
     * @param idBeneficio identificador del beneficio
     * @return true si ya cuenta con ese beneficio asignado
     */
    public boolean yaTieneBeneficio(int cedula, int idBeneficio) {
        String sql = "SELECT 1 FROM BeneficiosEstudiantes WHERE cedula = ? AND idBeneficio = ?";
        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setInt(1, cedula);
                pst.setInt(2, idBeneficio);
                try (ResultSet rs = pst.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar beneficio asignado: " + e.getMessage());
            return false;
        }
    }

    /**
     * Asigna un beneficio complementario a un estudiante.
     *
     * @param asig objeto POJO con la cédula y el idBeneficio
     * @return true si se insertó correctamente
     */
    public boolean asignarBeneficio(beneficiosEstudiantes asig) {
        String sql = "INSERT INTO BeneficiosEstudiantes (cedula, idBeneficio) VALUES (?, ?)";
        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setInt(1, asig.getCedula());
                pst.setInt(2, asig.getIdBeneficio());
                return pst.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al asignar beneficio al estudiante: " + e.getMessage());
            return false;
        }
    }

    /**
     * Quita (elimina) un beneficio asignado a un estudiante mediante el ID de asignación (PK).
     *
     * @param idBeneficioEstud llave primaria en la tabla BeneficiosEstudiantes
     * @return true si fue eliminado exitosamente
     */
    public boolean quitarBeneficio(int idBeneficioEstud) {
        String sql = "DELETE FROM BeneficiosEstudiantes WHERE idBeneficioEstud = ?";
        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setInt(1, idBeneficioEstud);
                return pst.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al quitar beneficio asignado: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene todos los beneficios asignados previamente a un estudiante específico.
     *
     * @param cedula cédula del estudiante
     * @return modelo de tabla con las asignaciones y montos
     */
    public DefaultTableModel mostrarBeneficiosDeEstudiante(int cedula) {
        String[] titulos = {"ID Asignación", "ID Beneficio", "Nombre del Beneficio", "Descripción", "Monto (₡)"};
        DefaultTableModel modelo = new DefaultTableModel(null, titulos) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String sql = "SELECT be.idBeneficioEstud, b.idBeneficio, b.nomBeneficio, b.descripcion, b.montoBeneficio "
                + "FROM BeneficiosEstudiantes be "
                + "INNER JOIN Beneficios b ON be.idBeneficio = b.idBeneficio "
                + "WHERE be.cedula = ? "
                + "ORDER BY be.idBeneficioEstud ASC";

        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return modelo;
            }
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setInt(1, cedula);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        Object[] fila = {
                            rs.getInt("idBeneficioEstud"),
                            rs.getInt("idBeneficio"),
                            rs.getString("nomBeneficio"),
                            rs.getString("descripcion"),
                            String.format(Locale.US, "%.2f", rs.getDouble("montoBeneficio"))
                        };
                        modelo.addRow(fila);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar beneficios del estudiante: " + e.getMessage());
        }
        return modelo;
    }

    /**
     * Suma el monto total de todos los beneficios asignados a un estudiante.
     *
     * @param cedula cédula del estudiante
     * @return sumatoria de montos en colones
     */
    public double obtenerTotalBeneficiosEstudiante(int cedula) {
        String sql = "SELECT COALESCE(SUM(b.montoBeneficio), 0) AS total "
                + "FROM BeneficiosEstudiantes be "
                + "INNER JOIN Beneficios b ON be.idBeneficio = b.idBeneficio "
                + "WHERE be.cedula = ?";
        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return 0.0;
            }
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setInt(1, cedula);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return rs.getDouble("total");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al calcular suma de beneficios: " + e.getMessage());
        }
        return 0.0;
    }
}
