
package pojo;

import java.time.LocalDate;

/**
 *
 * Clase que gestiona el registro y calculo de los pagos mensuales a estudiantes con beneficios.
 */
public class pagosMensuales {
     private int idPago;
     private LocalDate fechCreacion;
     private String mes;
     private LocalDate fechPago;
     private int estudiante; // cedula del estudiante (FK hacia estudiantes)
     private double totalBeneficios;
     private double deducSeguro; // 10 % de totalBeneficio
     private double deducRenta; // 5 % de totalBeneficio
     private double pagoNeto; // totalBeneficio - (deducSeguro + deducRenta)

    public pagosMensuales(int idPago, LocalDate fechCreacion, String mes, LocalDate fechPago, int estudiante, double totalBeneficios, double deducSeguro, double deducRenta, double pagoNeto) {
        this.idPago = idPago;
        this.fechCreacion = fechCreacion;
        this.mes = mes;
        this.fechPago = fechPago;
        this.estudiante = estudiante;
        this.totalBeneficios = totalBeneficios;
        this.deducSeguro = deducSeguro;
        this.deducRenta = deducRenta;
        this.pagoNeto = pagoNeto;
    }

    public pagosMensuales() {
        this.idPago = 0;
        this.fechCreacion = null;
        this.mes = "";
        this.fechPago = null;
        this.estudiante = 0;
        this.totalBeneficios = 0.0;
        this.deducSeguro = 0.0;
        this.deducRenta = 0.0;
        this.pagoNeto = 0.0;
    }

    public LocalDate getFechPago() {
        return fechPago;
    }

    public void setFechPago(LocalDate fechPago) {
        this.fechPago = fechPago;
    }

    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public LocalDate getFechCreacion() {
        return fechCreacion;
    }

    public void setFechCreacion(LocalDate fechCreacion) {
        this.fechCreacion = fechCreacion;
    }

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public int getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(int estudiante) {
        this.estudiante = estudiante;
    }

    public double getTotalBeneficios() {
        return totalBeneficios;
    }

    public void setTotalBeneficios(double totalBeneficios) {
        this.totalBeneficios = totalBeneficios;
    }

    public double getDeducSeguro() {
        return deducSeguro;
    }

    public void setDeducSeguro(double deducSeguro) {
        this.deducSeguro = deducSeguro;
    }

    public double getDeducRenta() {
        return deducRenta;
    }

    public void setDeducRenta(double deducRenta) {
        this.deducRenta = deducRenta;
    }

    public double getPagoNeto() {
        return pagoNeto;
    }

    public void setPagoNeto(double pagoNeto) {
        this.pagoNeto = pagoNeto;
    }
      
      /**
       * Calcula automaticamente las deducciones de seguro (10%),renta(5%) y el pago neto.
       * 
       */
    public final void calcularMontos(){
        this.deducSeguro = Math.round((this.totalBeneficios * 0.10) *100.0) /100.0;
         this.deducRenta = Math.round((this.totalBeneficios * 0.05) *100.0) /100.0;
         this.pagoNeto = Math.round((this.totalBeneficios - (this.deducSeguro + this.deducRenta)) * 100.0) / 100.0;
        
    }
}
