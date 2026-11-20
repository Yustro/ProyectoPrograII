/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pojo;

/**
 *
 * Clase que alamacena los beneficios complementarios que ofrece la Universidad.
 */
public class beneficios {

    private int idBeneficio;
    private String nomBeneficio;
    private String descripcion;
    private double montoBeneficio; // monto en colones con 2 decimales solamente

    public beneficios(int idBeneficio, String nomBeneficio, String descripcion, double montoBeneficio) {
        this.idBeneficio = idBeneficio;
        this.nomBeneficio = nomBeneficio;
        this.descripcion = descripcion;
        this.montoBeneficio = montoBeneficio;
    }

    public beneficios() {
        this.idBeneficio = 0;
        this.nomBeneficio = "";
        this.descripcion = "";
        this.montoBeneficio = 0.0;
    }

    public int getIdBeneficio() {
        return idBeneficio;
    }

    public void setIdBeneficio(int idBeneficio) {
        this.idBeneficio = idBeneficio;
    }

    public String getNomBeneficio() {
        return nomBeneficio;
    }

    public void setNomBeneficio(String nomBeneficio) {
        this.nomBeneficio = nomBeneficio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getMontoBeneficio() {
        return montoBeneficio;
    }

    public void setMontoBeneficio(double montoBeneficio) {
        this.montoBeneficio = montoBeneficio;
    }

    @Override
    public String toString() {
        return idBeneficio + " - " + nomBeneficio + "(₡" + String.format("%.2f",montoBeneficio) + ")";
        
    }
}
