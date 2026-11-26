package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.table.DefaultTableModel;
import pojo.beneficios;

/**
 * Modelo encargado de las operaciones CRUD en la tabla Beneficios.
 *
 * @author jeremydevon
 */
public class BeneficiosModel {

    /**
     * Verifica si ya existe un beneficio registrado con el idBeneficio indicado.
     *
     * @param idBeneficio identificador único del beneficio
     * @return true si ya existe, false en caso contrario
     */
    public boolean existeBeneficio(int idBeneficio) {
        String sql = "SELECT 1 FROM Beneficios WHERE idBeneficio = ?";
        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setInt(1, idBeneficio);
                try (ResultSet rs = pst.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar idBeneficio: " + e.getMessage());
            return false;
        }
    }

    /**
     * Inserta un nuevo beneficio complementario en la base de datos.
     *
     * @param ben objeto POJO con los datos del beneficio
     * @return true si se insertó correctamente
     */
    public boolean insertarBeneficio(beneficios ben) {
        String sql = "INSERT INTO Beneficios (idBeneficio, nomBeneficio, descripcion, montoBeneficio) VALUES (?, ?, ?, ?)";
        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setInt(1, ben.getIdBeneficio());
                pst.setString(2, ben.getNomBeneficio());
                pst.setString(3, ben.getDescripcion());
                pst.setDouble(4, ben.getMontoBeneficio());
                return pst.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar beneficio: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza la información de un beneficio existente.
     *
     * @param ben objeto POJO con los datos actualizados
     * @return true si se modificó correctamente
     */
    public boolean modificarBeneficio(beneficios ben) {
        String sql = "UPDATE Beneficios SET nomBeneficio = ?, descripcion = ?, montoBeneficio = ? WHERE idBeneficio = ?";
        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, ben.getNomBeneficio());
                pst.setString(2, ben.getDescripcion());
                pst.setDouble(3, ben.getMontoBeneficio());
                pst.setInt(4, ben.getIdBeneficio());
                return pst.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al modificar beneficio: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un beneficio según su identificador primario.
     *
     * @param idBeneficio identificador del beneficio a eliminar
     * @return true si se eliminó, false si ocurrió error o tiene asignaciones activas
     */
    public boolean eliminarBeneficio(int idBeneficio) {
        String sql = "DELETE FROM Beneficios WHERE idBeneficio = ?";
        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setInt(1, idBeneficio);
                return pst.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar beneficio (posibles estudiantes asignados): " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene todos los beneficios registrados para mostrarlos en tabla.
     *
     * @return modelo de tabla con los registros
     */
    public DefaultTableModel mostrarBeneficios() {
        return filtrarBeneficios("");
    }

    /**
     * Filtra beneficios por nombre, descripción o ID.
     *
     * @param textoBusqueda criterio de búsqueda
     * @return modelo de tabla filtrado y no editable
     */
    public DefaultTableModel filtrarBeneficios(String textoBusqueda) {
        String[] titulos = {"ID Beneficio", "Nombre", "Descripción", "Monto (₡)"};
        DefaultTableModel modelo = new DefaultTableModel(null, titulos) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String sql = "SELECT idBeneficio, nomBeneficio, descripcion, montoBeneficio FROM Beneficios "
                + "WHERE nomBeneficio LIKE ? OR descripcion LIKE ? OR CAST(idBeneficio AS CHAR) LIKE ? "
                + "ORDER BY idBeneficio ASC";

        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return modelo;
            }
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                String patron = "%" + textoBusqueda.trim() + "%";
                pst.setString(1, patron);
                pst.setString(2, patron);
                pst.setString(3, patron);

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        Object[] fila = {
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
            System.err.println("Error al listar/filtrar beneficios: " + e.getMessage());
        }
        return modelo;
    }

    /**
     * Lista todos los beneficios como objetos POJO para poblar un JComboBox.
     *
     * @return lista de beneficios ordenados por ID
     */
    public List<beneficios> listarBeneficiosCombo() {
        List<beneficios> lista = new ArrayList<>();
        String sql = "SELECT idBeneficio, nomBeneficio, descripcion, montoBeneficio FROM Beneficios ORDER BY idBeneficio ASC";

        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return lista;
            }
            try (PreparedStatement pst = con.prepareStatement(sql);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    lista.add(new beneficios(
                            rs.getInt("idBeneficio"),
                            rs.getString("nomBeneficio"),
                            rs.getString("descripcion"),
                            rs.getDouble("montoBeneficio")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar combo de beneficios: " + e.getMessage());
        }
        return lista;
    }
}
