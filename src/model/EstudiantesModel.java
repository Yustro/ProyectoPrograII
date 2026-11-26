package model;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import pojo.estudiante;

/**
 * Modelo encargado de las operaciones CRUD y validaciones de unicidad en la tabla Estudiante.
 *
 * @author jeremydevon
 */
public class EstudiantesModel {

    /**
     * Verifica si ya existe un estudiante con el carnet indicado (PK).
     *
     * @param carnet carnet único del estudiante
     * @return true si ya está registrado
     */
    public boolean existeCarnet(String carnet) {
        String sql = "SELECT 1 FROM Estudiante WHERE carnet = ?";
        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, carnet.trim());
                try (ResultSet rs = pst.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar carnet: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si una cédula ya pertenece a otro estudiante (restricción UNIQUE).
     *
     * @param cedula número de cédula a validar
     * @param excluirCarnet carnet actual al editar (o vacío "" al insertar)
     * @return true si otra fila ya usa esa cédula
     */
    public boolean existeCedula(int cedula, String excluirCarnet) {
        String sql = "SELECT 1 FROM Estudiante WHERE cedula = ? AND carnet <> ?";
        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setInt(1, cedula);
                pst.setString(2, excluirCarnet == null ? "" : excluirCarnet.trim());
                try (ResultSet rs = pst.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar cédula: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si un correo electrónico ya pertenece a otro estudiante (restricción UNIQUE).
     *
     * @param email correo electrónico a validar
     * @param excluirCarnet carnet actual al editar (o vacío "" al insertar)
     * @return true si otra fila ya usa ese email
     */
    public boolean existeEmail(String email, String excluirCarnet) {
        String sql = "SELECT 1 FROM Estudiante WHERE LOWER(email) = LOWER(?) AND carnet <> ?";
        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, email.trim());
                pst.setString(2, excluirCarnet == null ? "" : excluirCarnet.trim());
                try (ResultSet rs = pst.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar email: " + e.getMessage());
            return false;
        }
    }

    /**
     * Inserta un nuevo estudiante en la base de datos manejando fechEgreso opcional (NULL).
     *
     * @param est objeto POJO estudiante con la información completa
     * @return true si el registro fue exitoso
     */
    public boolean insertarEstudiante(estudiante est) {
        String sql = "INSERT INTO Estudiante "
                + "(carnet, cedula, nombre, fechNac, direccion, telefono, email, fechIngreso, fechEgreso, idCarrera) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, est.getCarnet().trim());
                pst.setInt(2, est.getCedula());
                pst.setString(3, est.getNombre().trim());
                pst.setDate(4, Date.valueOf(est.getFechNac()));
                pst.setString(5, est.getDireccion().trim());
                pst.setString(6, est.getTelefono().trim());
                pst.setString(7, est.getEmail().trim());
                pst.setDate(8, Date.valueOf(est.getFechIngreso()));
                if (est.getFechEgreso() != null) {
                    pst.setDate(9, Date.valueOf(est.getFechEgreso()));
                } else {
                    pst.setNull(9, Types.DATE);
                }
                pst.setInt(10, est.getIdCarrera());
                return pst.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al insertar estudiante: " + e.getMessage());
            return false;
        }
    }

    /**
     * Modifica los datos de un estudiante existente identificado por su carnet.
     *
     * @param est objeto POJO estudiante actualizado
     * @return true si la actualización fue exitosa
     */
    public boolean modificarEstudiante(estudiante est) {
        String sql = "UPDATE Estudiante SET "
                + "cedula = ?, nombre = ?, fechNac = ?, direccion = ?, telefono = ?, "
                + "email = ?, fechIngreso = ?, fechEgreso = ?, idCarrera = ? "
                + "WHERE carnet = ?";
        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setInt(1, est.getCedula());
                pst.setString(2, est.getNombre().trim());
                pst.setDate(3, Date.valueOf(est.getFechNac()));
                pst.setString(4, est.getDireccion().trim());
                pst.setString(5, est.getTelefono().trim());
                pst.setString(6, est.getEmail().trim());
                pst.setDate(7, Date.valueOf(est.getFechIngreso()));
                if (est.getFechEgreso() != null) {
                    pst.setDate(8, Date.valueOf(est.getFechEgreso()));
                } else {
                    pst.setNull(8, Types.DATE);
                }
                pst.setInt(9, est.getIdCarrera());
                pst.setString(10, est.getCarnet().trim());
                return pst.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al modificar estudiante: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un estudiante por su carnet primario.
     *
     * @param carnet carnet del estudiante a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean eliminarEstudiante(String carnet) {
        String sql = "DELETE FROM Estudiante WHERE carnet = ?";
        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, carnet.trim());
                return pst.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al eliminar estudiante (posibles pagos registrados): " + e.getMessage());
            return false;
        }
    }

    /**
     * Muestra todos los estudiantes junto con el detalle de su carrera.
     *
     * @return modelo de tabla con todos los registros
     */
    public DefaultTableModel mostrarEstudiantes() {
        return filtrarEstudiantes("");
    }

    /**
     * Filtra estudiantes por nombre, carnet, cédula o carrera haciendo JOIN con Carreras.
     *
     * @param textoBusqueda texto de filtro
     * @return modelo de tabla no editable
     */
    public DefaultTableModel filtrarEstudiantes(String textoBusqueda) {
        String[] titulos = {
            "Carnet", "Cédula", "Nombre", "Fecha Nac", "Dirección",
            "Teléfono", "Email", "Fecha Ingreso", "Fecha Egreso", "Carrera"
        };
        DefaultTableModel modelo = new DefaultTableModel(null, titulos) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String sql = "SELECT e.carnet, e.cedula, e.nombre, e.fechNac, e.direccion, "
                + "e.telefono, e.email, e.fechIngreso, e.fechEgreso, "
                + "e.idCarrera, c.nomCarrera, c.grado "
                + "FROM Estudiante e "
                + "INNER JOIN Carreras c ON e.idCarrera = c.idCarrera "
                + "WHERE e.nombre LIKE ? OR e.carnet LIKE ? "
                + "OR CAST(e.cedula AS CHAR) LIKE ? OR c.nomCarrera LIKE ? "
                + "ORDER BY e.nombre ASC";

        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return modelo;
            }
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                String patron = "%" + textoBusqueda.trim() + "%";
                pst.setString(1, patron);
                pst.setString(2, patron);
                pst.setString(3, patron);
                pst.setString(4, patron);

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        Date fechaEg = rs.getDate("fechEgreso");
                        String carreraTexto = rs.getInt("idCarrera") + " - "
                                + rs.getString("nomCarrera") + " (" + rs.getString("grado") + ")";
                        Object[] fila = {
                            rs.getString("carnet"),
                            rs.getInt("cedula"),
                            rs.getString("nombre"),
                            rs.getDate("fechNac").toString(),
                            rs.getString("direccion"),
                            rs.getString("telefono"),
                            rs.getString("email"),
                            rs.getDate("fechIngreso").toString(),
                            fechaEg != null ? fechaEg.toString() : "",
                            carreraTexto
                        };
                        modelo.addRow(fila);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar/filtrar estudiantes: " + e.getMessage());
        }
        return modelo;
    }

    /**
     * Obtiene la lista de estudiantes para cargar en el JComboBox de Asignar Beneficios.
     *
     * @return lista de objetos estudiante
     */
    public List<estudiante> listarEstudiantesCombo() {
        List<estudiante> lista = new ArrayList<>();
        String sql = "SELECT carnet, cedula, nombre, fechNac, direccion, telefono, email, "
                + "fechIngreso, fechEgreso, idCarrera FROM Estudiante ORDER BY nombre ASC";

        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return lista;
            }
            try (PreparedStatement pst = con.prepareStatement(sql);
                 ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Date eg = rs.getDate("fechEgreso");
                    LocalDate localEg = eg != null ? eg.toLocalDate() : null;
                    lista.add(new estudiante(
                            rs.getString("carnet"),
                            rs.getDate("fechIngreso").toLocalDate(),
                            localEg,
                            rs.getInt("idCarrera"),
                            rs.getInt("cedula"),
                            rs.getString("nombre"),
                            rs.getDate("fechNac").toLocalDate(),
                            rs.getString("direccion"),
                            rs.getString("telefono"),
                            rs.getString("email")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar combo de estudiantes: " + e.getMessage());
        }
        return lista;
    }
}
