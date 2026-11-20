/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pojo;

/**
 *
 * Clase intermedia para los beneficios asignados a cada estudiante.
 */
public class beneficiosEstudiantes {
    private int idBeneficioEstud ; // PK en tabla intermedia
    private int cedula; //FK hacia Estudiantes (cedula)
    private int idBeneficio; //FK hacia beneficios (idBeneficio)

    public beneficiosEstudiantes(int idBeneficioEstud, int cedula, int idBeneficio) {
        this.idBeneficioEstud = idBeneficioEstud;
        this.cedula = cedula;
        this.idBeneficio = idBeneficio;
    }
    
    
     public beneficiosEstudiantes() {
        this.idBeneficioEstud = 0;
        this.cedula = 0;
        this.idBeneficio = 0;
        
    }

    public int getIdBeneficioEstud() {
        return idBeneficioEstud;
    }

    public void setIdBeneficioEstud(int idBeneficioEstud) {
        this.idBeneficioEstud = idBeneficioEstud;
    }

    public int getCedula() {
        return cedula;
    }

    public void setCedula(int cedula) {
        this.cedula = cedula;
    }

    public int getIdBeneficio() {
        return idBeneficio;
    }

    public void setIdBeneficio(int idBeneficio) {
        this.idBeneficio = idBeneficio;
    }
    
    
}
