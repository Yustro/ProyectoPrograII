package model;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.table.DefaultTableModel;
import pojo.pagosMensuales;

/**
 * Modelo encargado de la generación, consulta y filtrado de planillas en PagosMensuales.
 *
 * @author jeremydevon
 */
public class PagosMensualesModel {

    /**
     * Verifica si ya fue generada una planilla para el mes y año indicados.
     *
     * @param mes nombre del mes en texto (ej. "Noviembre")
     * @param anio año de la planilla (ej. 2026)
     * @return true si ya existe al menos un registro para ese mes/año
     */
    public boolean existePlanillaMesAnio(String mes, int anio) {
        String sql = "SELECT 1 FROM PagosMensuales WHERE LOWER(mes) = LOWER(?) AND YEAR(fechPago) = ? LIMIT 1";
        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return false;
            }
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, mes.trim());
                pst.setInt(2, anio);
                try (ResultSet rs = pst.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar existencia de planilla: " + e.getMessage());
            return false;
        }
    }

    /**
     * Genera la planilla mensual completa insertando un registro por cada estudiante
     * que posea beneficios asignados, aplicando los cálculos automáticos de ley.
     *
     * @param mes nombre del mes en texto
     * @param fechaCreacion fecha actual del sistema
     * @param fechaPago fecha efectiva de pago del periodo
     * @return cantidad de estudiantes procesados en la planilla, o -1 si ocurrió un error
     */
    public int generarPlanillaMensual(String mes, LocalDate fechaCreacion, LocalDate fechaPago) {
        String sqlEstudiantesBeneficiados = "SELECT be.cedula, SUM(b.montoBeneficio) AS totalBeneficios "
                + "FROM BeneficiosEstudiantes be "
                + "INNER JOIN Beneficios b ON be.idBeneficio = b.idBeneficio "
                + "GROUP BY be.cedula "
                + "HAVING SUM(b.montoBeneficio) > 0";

        String sqlInsertPago = "INSERT INTO PagosMensuales "
                + "(fechCreacion, mes, fechPago, estudiante, totalBeneficios, deducSeguro, deducRenta, pagoNeto) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        List<pagosMensuales> registrosAGenerar = new ArrayList<>();

        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return -1;
            }

            try (PreparedStatement pstConsulta = con.prepareStatement(sqlEstudiantesBeneficiados);
                 ResultSet rs = pstConsulta.executeQuery()) {
                while (rs.next()) {
                    pagosMensuales pago = new pagosMensuales();
                    pago.setFechCreacion(fechaCreacion);
                    pago.setMes(mes);
                    pago.setFechPago(fechaPago);
                    pago.setEstudiante(rs.getInt("cedula"));
                    pago.setTotalBeneficios(rs.getDouble("totalBeneficios"));
                    pago.calcularMontos(); // Calcula 10% seguro, 5% renta y pago neto
                    registrosAGenerar.add(pago);
                }
            }

            if (registrosAGenerar.isEmpty()) {
                return 0;
            }

            con.setAutoCommit(false);
            try (PreparedStatement pstInsert = con.prepareStatement(sqlInsertPago)) {
                for (pagosMensuales p : registrosAGenerar) {
                    pstInsert.setDate(1, Date.valueOf(p.getFechCreacion()));
                    pstInsert.setString(2, p.getMes());
                    pstInsert.setDate(3, Date.valueOf(p.getFechPago()));
                    pstInsert.setInt(4, p.getEstudiante());
                    pstInsert.setDouble(5, p.getTotalBeneficios());
                    pstInsert.setDouble(6, p.getDeducSeguro());
                    pstInsert.setDouble(7, p.getDeducRenta());
                    pstInsert.setDouble(8, p.getPagoNeto());
                    pstInsert.addBatch();
                }
                pstInsert.executeBatch();
                con.commit();
                return registrosAGenerar.size();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error al generar planilla mensual: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Muestra todos los pagos registrados en el sistema.
     *
     * @return modelo de tabla con el historial completo
     */
    public DefaultTableModel mostrarTodosLosPagos() {
        return filtrarPagos("", false, false, false);
    }

    /**
     * Filtra los registros de pagos según los JCheckBox seleccionados (Estudiante, Mes, Año).
     *
     * @param texto texto digitado en el buscador
     * @param porEstudiante true si el JCheckBox Estudiante está marcado
     * @param porMes true si el JCheckBox Mes está marcado
     * @param porAnio true si el JCheckBox Año está marcado
     * @return modelo de tabla con los pagos filtrados
     */
    public DefaultTableModel filtrarPagos(String texto, boolean porEstudiante, boolean porMes, boolean porAnio) {
        String[] titulos = {
            "ID Pago", "Fecha Creación", "Mes", "Año", "Fecha Pago",
            "Estudiante", "Total Beneficios", "Deduc. Seguro (10%)", "Deduc. Renta (5%)", "Pago Neto"
        };
        DefaultTableModel modelo = new DefaultTableModel(null, titulos) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String filtro = texto == null ? "" : texto.trim();
        boolean algunoMarcado = porEstudiante || porMes || porAnio;

        StringBuilder sql = new StringBuilder(
                "SELECT p.idPago, p.fechCreacion, p.mes, YEAR(p.fechPago) AS anio, p.fechPago, "
                + "p.estudiante, e.nombre, p.totalBeneficios, p.deducSeguro, p.deducRenta, p.pagoNeto "
                + "FROM PagosMensuales p "
                + "INNER JOIN Estudiante e ON p.estudiante = e.cedula ");

        List<String> condiciones = new ArrayList<>();
        if (!filtro.isEmpty()) {
            if (!algunoMarcado || porEstudiante) {
                condiciones.add("(e.nombre LIKE ? OR CAST(p.estudiante AS CHAR) LIKE ?)");
            }
            if (!algunoMarcado || porMes) {
                condiciones.add("p.mes LIKE ?");
            }
            if (!algunoMarcado || porAnio) {
                condiciones.add("CAST(YEAR(p.fechPago) AS CHAR) LIKE ?");
            }
            sql.append("WHERE ").append(String.join(" OR ", condiciones)).append(" ");
        }
        sql.append("ORDER BY p.idPago DESC");

        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return modelo;
            }
            try (PreparedStatement pst = con.prepareStatement(sql.toString())) {
                if (!filtro.isEmpty()) {
                    String patron = "%" + filtro + "%";
                    int idx = 1;
                    if (!algunoMarcado || porEstudiante) {
                        pst.setString(idx++, patron);
                        pst.setString(idx++, patron);
                    }
                    if (!algunoMarcado || porMes) {
                        pst.setString(idx++, patron);
                    }
                    if (!algunoMarcado || porAnio) {
                        pst.setString(idx++, patron);
                    }
                }

                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        Object[] fila = {
                            rs.getInt("idPago"),
                            rs.getDate("fechCreacion").toString(),
                            rs.getString("mes"),
                            rs.getInt("anio"),
                            rs.getDate("fechPago").toString(),
                            rs.getInt("estudiante") + " - " + rs.getString("nombre"),
                            String.format(Locale.US, "%.2f", rs.getDouble("totalBeneficios")),
                            String.format(Locale.US, "%.2f", rs.getDouble("deducSeguro")),
                            String.format(Locale.US, "%.2f", rs.getDouble("deducRenta")),
                            String.format(Locale.US, "%.2f", rs.getDouble("pagoNeto"))
                        };
                        modelo.addRow(fila);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar/filtrar pagos mensuales: " + e.getMessage());
        }
        return modelo;
    }

    /**
     * Obtiene todos los registros de una planilla específica por mes y año para DlgPlanillasView.
     *
     * @param mes nombre del mes elegido
     * @param anio año elegido
     * @return modelo de tabla con el detalle de esa planilla
     */
    public DefaultTableModel obtenerPlanillaPorMesAnio(String mes, int anio) {
        String[] titulos = {
            "ID Pago", "Cédula", "Estudiante", "Carnet", "Fecha Pago",
            "Total Beneficios", "Deduc. Seguro", "Deduc. Renta", "Pago Neto"
        };
        DefaultTableModel modelo = new DefaultTableModel(null, titulos) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String sql = "SELECT p.idPago, p.estudiante, e.nombre, e.carnet, p.fechPago, "
                + "p.totalBeneficios, p.deducSeguro, p.deducRenta, p.pagoNeto "
                + "FROM PagosMensuales p "
                + "INNER JOIN Estudiante e ON p.estudiante = e.cedula "
                + "WHERE LOWER(p.mes) = LOWER(?) AND YEAR(p.fechPago) = ? "
                + "ORDER BY e.nombre ASC";

        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return modelo;
            }
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, mes.trim());
                pst.setInt(2, anio);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        Object[] fila = {
                            rs.getInt("idPago"),
                            rs.getInt("estudiante"),
                            rs.getString("nombre"),
                            rs.getString("carnet"),
                            rs.getDate("fechPago").toString(),
                            String.format(Locale.US, "%.2f", rs.getDouble("totalBeneficios")),
                            String.format(Locale.US, "%.2f", rs.getDouble("deducSeguro")),
                            String.format(Locale.US, "%.2f", rs.getDouble("deducRenta")),
                            String.format(Locale.US, "%.2f", rs.getDouble("pagoNeto"))
                        };
                        modelo.addRow(fila);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener planilla mensual específica: " + e.getMessage());
        }
        return modelo;
    }

    /**
     * Calcula los totales generales de una planilla específica: [0]=Total Beneficios,
     * [1]=Total Deducciones (Seguro + Renta), [2]=Total Neto.
     *
     * @param mes nombre del mes elegido
     * @param anio año elegido
     * @return arreglo double de 3 posiciones con las sumatorias
     */
    public double[] obtenerTotalesPlanillaMesAnio(String mes, int anio) {
        double[] totales = {0.0, 0.0, 0.0};
        String sql = "SELECT COALESCE(SUM(totalBeneficios), 0) AS sumBenef, "
                + "COALESCE(SUM(deducSeguro + deducRenta), 0) AS sumDeduc, "
                + "COALESCE(SUM(pagoNeto), 0) AS sumNeto "
                + "FROM PagosMensuales "
                + "WHERE LOWER(mes) = LOWER(?) AND YEAR(fechPago) = ?";

        try (Connection con = DataBaseConnection.conectar()) {
            if (con == null) {
                return totales;
            }
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, mes.trim());
                pst.setInt(2, anio);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        totales[0] = rs.getDouble("sumBenef");
                        totales[1] = rs.getDouble("sumDeduc");
                        totales[2] = rs.getDouble("sumNeto");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al calcular totales de planilla: " + e.getMessage());
        }
        return totales;
    }
}
