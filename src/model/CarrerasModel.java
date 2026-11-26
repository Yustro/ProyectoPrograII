/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import pojo.carreras;

/**
 * Va a verificar si ya existe una carrera con ese idCarreras
 */
  

    public class CarrerasModel {

        public boolean existeCarrera(int idCarrera) {
            String sql = "SELECT 1 FROM Carreras WHERE idCarrera = ?";

            try (Connection con = DataBaseConnection.conectar()) {
                if (con == null) {
                    return false;
                }
                try (PreparedStatement pst = con.prepareStatement(sql)) {
                    pst.setInt(1, idCarrera); //Reemplaza el primer '?' por el idCarrera 
                    try (ResultSet rs = pst.executeQuery()) {
                        return rs.next(); // si rs es.next() es true significa que ya existe.
                    }
                }

            } catch (SQLException e) {
                System.err.println("Error al verificar idCarrera" + e.getMessage());
                return false;

            }
        }

        /**
         * Insertamos una nueva carrera a la base de datos
         */
        public boolean insertarCarrera(carreras car) {
            String sql = "INSERT INTO Carreras (idCarrera, nomCarrera, grado) VALUES (?, ?, ?)";
            try (Connection con = DataBaseConnection.conectar()) {
                if (con == null) {
                    return false;
                }
                try (PreparedStatement pst = con.prepareStatement(sql)) {
                    pst.setInt(1, car.getIdCarrera());
                    pst.setString(2, car.getNomCarrera());
                    pst.setString(3, car.getGrado());
                    return pst.executeUpdate() > 0;
                }
            } catch (SQLException e) {
                System.err.println("Error al insertar carrera: " + e.getMessage());
                return false;
            }
        }

        /**
         *
         * modifica el nombre y el grado de una carrera ya existente
         */
        public boolean modificarCarrera(carreras car) {
            String sql = "UPDATE Carreras SET nomCarrera = ?, grado = ? WHERE idCarrera = ?";
            try (Connection con = DataBaseConnection.conectar()) {
                if (con == null) {
                    return false;
                }
                try (PreparedStatement pst = con.prepareStatement(sql)) {
                    pst.setString(1, car.getNomCarrera());
                    pst.setString(2, car.getGrado());
                    pst.setInt(3, car.getIdCarrera()); // El WHERE va en el '?' número 3
                    return pst.executeUpdate() > 0;
                }
            } catch (SQLException e) {
                System.err.println("Error al modificar carrera: " + e.getMessage());
                return false;
            }
        }

        /**
         * Elimina una carrera por su idCarrera
         *
         * @param idCarrera
         * @return
         */
        public boolean eliminarCarrera(int idCarrera) {
            String sql = "DELETE FROM Carreras WHERE idCarrera = ?";
            try (Connection con = DataBaseConnection.conectar()) {
                if (con == null) {
                    return false;
                }
                try (PreparedStatement pst = con.prepareStatement(sql)) {
                    pst.setInt(1, idCarrera);
                    return pst.executeUpdate() > 0;
                }
            } catch (SQLException e) {
                System.err.println("Error al eliminar carrera (posibles estudiantes asociados): " + e.getMessage());
                return false;
            }
        }

        public DefaultTableModel mostrarCarreras() {
            return filtrarCarreras("");
        }

        public DefaultTableModel filtrarCarreras(String textoBusqueda) {
            String[] titulo = {"ID Carrera", "Nombre de carrera", "Grado Academico"};
            DefaultTableModel modelo = new DefaultTableModel(null, titulo) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
    
 String sql = "SELECT idCarrera, nomCarrera, grado FROM Carreras "
                + "WHERE nomCarrera LIKE ? OR grado LIKE ? OR CAST(idCarrera AS CHAR) LIKE ? "
                + "ORDER BY idCarrera ASC";
    

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
                            rs.getInt("idCarrera"),
                            rs.getString("nomCarrera"),
                            rs.getString("grado")
                        };
                        modelo.addRow(fila);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar/filtrar carreras: " + e.getMessage());
        }
        return modelo;
    }
    /**
     * Devuelve una lista de objetos carreras para llenar el JComboBox en Estudiante.
     */
    public List<carreras> listarCarrerasCombo() {
        List<carreras> lista = new ArrayList<>();
        String sql = "SELECT idCarrera, nomCarrera, grado FROM Carreras ORDER BY idCarrera ASC";
        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return lista;
            }
            try (PreparedStatement pst = con.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    lista.add(new carreras(
                            rs.getInt("idCarrera"),
                            rs.getString("nomCarrera"),
                            rs.getString("grado")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar combo de carreras: " + e.getMessage());
        }
        return lista;

    }
}
