/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pojo;

/**
 *
 * Clase que almacenar toda la informacion referente a las carerras que ofrece la UTN.
 */
public class carreras {
    private int idCarrera;
    private String nomCarrera;
    private String grado; // Diplomado,Bachiller,Maestria.

    public carreras(int idCarrera, String nomCarrera, String grado) {
        this.idCarrera = idCarrera;
        this.nomCarrera = nomCarrera;
        this.grado = grado;
    }
    
     public carreras() {
        this.idCarrera = 0;
        this.nomCarrera = "";
        this.grado = "";
    }

    public int getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(int idCarrera) {
        this.idCarrera = idCarrera;
    }

    public String getNomCarrera() {
        return nomCarrera;
    }

    public void setNomCarrera(String nomCarrera) {
        this.nomCarrera = nomCarrera;
    }

    public String getGrado() {
        return grado;
    }

    public void setGrado(String grado) {
        this.grado = grado;
    }
    
}
